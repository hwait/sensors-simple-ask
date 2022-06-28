package sensorsapp

import pureconfig._
import pureconfig.generic.auto._

package object utils {

  case class RandomGeneratorConstrains(lower: Int, upper: Int)
  case class ReferenceRandomGeneratorConstrains(
      lower: Int,
      upper: Int,
      items: Int
  )
  case class Variances(length: Float, deviation: Float)

  val randomGeneratorConstraits = ConfigSource.default
    .at("random-generator-constraits")
    .loadOrThrow[RandomGeneratorConstrains]
  val referenceConstraits = ConfigSource.default
    .at("reference-generator-constraits")
    .loadOrThrow[ReferenceRandomGeneratorConstrains]
  val variances = ConfigSource.default.at("variance").loadOrThrow[Variances]
}
