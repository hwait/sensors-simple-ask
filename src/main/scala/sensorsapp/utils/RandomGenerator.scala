package sensorsapp.utils

import scala.util.Random

object RandomGenerator {

  // def random() = new scala.util.Random(seed)

  def generate: Seq[Int] = {
    val itemsNumber: Int = Random.between(
      (referenceConstraits.items * (1 - variances.length)).toInt,
      (referenceConstraits.items * (1 + variances.length)).toInt
    )
    Seq.fill(itemsNumber)(
      Random.between(
        randomGeneratorConstraits.lower,
        randomGeneratorConstraits.upper + 1
      )
    )
  }
  def generateReferenceSequence: Seq[Int] =
    Seq.fill(referenceConstraits.items)(
      Random.between(
        referenceConstraits.lower,
        referenceConstraits.upper + 1
      )
    )

  def main(args: Array[String]): Unit = {
    println(generate)
    println(generateReferenceSequence)
  }
}
