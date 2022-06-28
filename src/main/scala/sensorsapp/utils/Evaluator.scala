package sensorsapp.utils

import scala.annotation.tailrec
import scala.math.pow

object Evaluator {
  def evaluate(numbers: Seq[Int]): (Int, Int) = 
    if (numbers.size==0) (0, 0)
    else {
      val mean = numbers.sum / numbers.size
      val stddev =
        math
          .sqrt(numbers.map(n => math.pow(n - mean, 2)).sum / numbers.size)
          .toInt
      (mean, stddev)
    }
}
