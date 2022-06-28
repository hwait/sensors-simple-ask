package sensorsapp.actors

import akka.actor.{Actor, ActorLogging}
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import sensorsapp.domain.{Sensor, Command}
import sensorsapp.domain.Commands._
import sensorsapp.utils.{RandomGenerator, Evaluator}

object SensorActor {

  def apply(): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match {
        case CreateSensor(name, replyTo) =>
          val numbers = if (name == "sensor_reference") RandomGenerator.generateReferenceSequence else RandomGenerator.generate
          val (mean, stddev) = Evaluator.evaluate(numbers)
          context.log.info(s"[SensorActor]: created sensor $name")
          replyTo ! SensorCreated(Sensor(name, 0, numbers, mean, stddev))

          Behaviors.stopped
        case _ => Behaviors.unhandled
      }
    }
}
