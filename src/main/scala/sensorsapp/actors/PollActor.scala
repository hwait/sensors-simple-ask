package sensorsapp.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.actor.typed.{Behavior, ActorRef}
import akka.actor.typed.scaladsl.Behaviors
import sensorsapp.domain.{Sensor, Command, Threshold}
import sensorsapp.domain.Commands._
import sensorsapp.utils._

object PollActor {

  def thresholdGetOrCreate(option: Option[Sensor]): Threshold =
    option match {
      case Some(value) =>
        Threshold(value.numbers.size, value.mean, value.stddev)
      case None =>
        Threshold(
          referenceConstraits.items,
          (referenceConstraits.upper + referenceConstraits.lower) / 2,
          0
        )
    }

  def filterSensors(sensor: Sensor, threshold: Threshold): Boolean = {
    sensor.numbers.size > threshold.length * (1 + variances.length) ||
    sensor.numbers.size < threshold.length * (1 - variances.length) ||
    sensor.stddev > threshold.stddev * (1 + variances.deviation) ||
    sensor.stddev < threshold.stddev * (1 - variances.deviation)
  }

  def apply(): Behavior[Command] = initPoll()

  def initPoll(): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match {
        case CreatePoll(nSensors, replyTo) =>
          context.log.info(s"CreatePoll of $nSensors sensors")
          (1 to nSensors).foreach(i =>
            context.spawn(SensorActor(), s"sensor_$i") ! CreateSensor(
              i.toString,
              context.self
            )
          )
          context.spawn(SensorActor(), s"sensor_reference") ! CreateSensor(
            "reference",
            context.self
          )
          createSensors(nSensors, List(), None, replyTo)
        case _ => Behaviors.unhandled
      }
    }

  def createSensors(
      restSensors: Int,
      sensors: List[Sensor],
      reference: Option[Sensor],
      replyTo: ActorRef[Command]
  ): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      def scorePoll(
          sensors: List[Sensor],
          reference: Option[Sensor]
      ): List[Sensor] = {
        val threshold = thresholdGetOrCreate(reference)
        sensors
          .filter(sensor => filterSensors(sensor, threshold))
      }
      message match {
        case SensorCreated(sensor) =>
          context.log.info(
            s"[PollActor createSensors $restSensors, ${sensors.length}, $reference] received sensor ${sensor.name}"
          )
          (restSensors > 0, sensor.name == "reference") match {
            case (true, false) =>
              createSensors(
                restSensors - 1,
                sensor :: sensors,
                reference,
                replyTo
              )
            case (true, true) =>
              createSensors(restSensors - 1, sensors, Some(sensor), replyTo)
            case (false, false) =>
              context.self ! CalculatePoll(sensor :: sensors, reference)
              Behaviors.same
            case (false, true) =>
              context.self ! CalculatePoll(sensors, Some(sensor))
              Behaviors.same
          }
        case CalculatePoll(sensors, reference) =>
          val threshold = thresholdGetOrCreate(reference)
          val filteredSensors =
            sensors
              .filter(sensor => filterSensors(sensor, threshold))
              .sortWith((a, b) => a.stddev > b.stddev)
              .zipWithIndex
              .map(pair => pair._1.copy(id = pair._2 + 1))

          val result = reference match {
            case None        => filteredSensors
            case Some(value) => value :: filteredSensors
          }
          replyTo ! PollResult(result)
          initPoll()
        case _ => Behaviors.unhandled
      }
    }

}
