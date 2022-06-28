package sensorsapp.domain

final case class Sensor(name: String, id: Int, numbers: Seq[Int], mean: Int, stddev: Int)
