package sensorsapp.domain

import akka.actor.typed.ActorRef

sealed trait Command

object Commands {
  case class CreatePoll(nSensors: Int, replyTo: ActorRef[Command])
      extends Command
  case class CreateSensor(name: String, replyTo: ActorRef[Command])
      extends Command
  case class SensorCreated(sensor: Sensor) extends Command
  case class CalculatePoll(sensors: List[Sensor], reference: Option[Sensor])
      extends Command
  case class RunPoll(poll: Poll) extends Command
  case class PollResult(sensors: List[Sensor]) extends Command
}
