package sensorsapp.server

import cats.data.ValidatedNel
import cats.implicits._

object Validation {
  trait Exactly[A] extends ((A, A) => Boolean)
  trait Minimum[A] extends ((A, Double) => Boolean)

  implicit val exactlyString: Exactly[String] = _ == _
  implicit val minimumInt: Minimum[Int] = _ >= _
  implicit val minimumLong: Minimum[Long] = _ >= _

  def exactly[A](value: A, pattern: A)(implicit exact: Exactly[A]): Boolean = exact(value, pattern)
  def minimum[A](value: A, threshold: Double)(implicit min: Minimum[A]): Boolean = min(value, threshold)

  trait ValidationFailure {
    def errorMessage: String
  }
  type ValidationResult[A] = ValidatedNel[ValidationFailure, A]

  // Messages:
  case class WrongFieldValue[A](fieldName: String, pattern:A) extends ValidationFailure {
    override def errorMessage: String = s"Wrong $fieldName value (required \"$pattern\")"
  }
  case class BelowMinimumValue(fieldName: String, min: Double) extends ValidationFailure {
    override def errorMessage = s"$fieldName is below the minimum threshold $min"
  }

  case class NegativeValue(fieldName: String) extends ValidationFailure {
    override def errorMessage = s"$fieldName is negative"
  }

  // logic:
  def validateMinimum[A: Minimum](value: A, threshold: Double, fieldName: String): ValidationResult[A] = {
    if (minimum(value, threshold)) value.validNel
    else if (threshold == 0) NegativeValue(fieldName).invalidNel
    else BelowMinimumValue(fieldName, threshold).invalidNel
  }

  def validateExact[A: Exactly](value: A, pattern: A, fieldName: String): ValidationResult[A] =
    if (exactly(value, pattern)) value.validNel
    else WrongFieldValue(fieldName, pattern).invalidNel

  trait Validator[A] {
    def validate(value: A): ValidationResult[A]
  }

  def validateEntity[A](value: A)(implicit validator: Validator[A]): ValidationResult[A] =
    validator.validate(value)
}
