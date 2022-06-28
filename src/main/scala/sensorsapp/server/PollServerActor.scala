package sensorsapp.server

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Success, Failure}
import akka.http.scaladsl.model.StatusCodes
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.ActorRef
import akka.util.Timeout
import io.circe.parser.decode
import io.circe.generic.auto._
import io.circe.syntax._
import sensorsapp.domain.Command
import sensorsapp.domain.Commands._
import Validation._
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._

object PollServerActor {
  implicit val timeout: Timeout = Timeout(5.seconds)

  def apply(pollActor: ActorRef[Command]): Behavior[ServerMessage] =
    Behaviors.receive { (context, message) =>
      implicit val scheduler = context.system.scheduler
      implicit val ec = context.system.executionContext
      message match {
        case MessageWithCallback(
              replyTo,
              msg
            ) => 
          def validateRequest[ServerMessage: Validator](
              request: ServerMessage
          )(validAction: ServerMessage => Unit): Unit =
            validateEntity(request) match {
              case Valid(_) => validAction(request)
              case Invalid(failures) =>
                replyTo ! MessageBack(
                  failures.toList.map(_.errorMessage).mkString(",")
                )
            }

          decode[PollRequest](msg) match {
            case Right(
                  request @ PollRequest(messageType, sensors)
                ) =>
              validateRequest(request) { _ =>
                val pollResultFuture = pollActor.ask(askReply =>
                  CreatePoll(sensors, askReply)
                )
                pollResultFuture.onComplete {
                  case Success(pollResult: PollResult) =>
                    replyTo ! MessageBack(
                      PollResponse(pollResult).asJson.toString
                    )
                  case Failure(exception) =>
                    replyTo ! MessageBack(
                      s"PollRequest $messageType to poll for $sensors has failed with the exception: ${exception.getMessage}" 
                    )
                  case _ => replyTo ! MessageBack("Wrong poll result")
                }
              }
              Behaviors.same

            case Left(_) =>
              decode[PingRequest](msg) match {
                case Right(
                      request @ PingRequest(messageType, id, ts)
                    ) => 
                  validateRequest(request) { _ =>
                    val currentTs = System.currentTimeMillis / 1000
                    val pong = PongResponse(
                      message_type = "pong",
                      request_id = id,
                      request_at = ts,
                      ts = currentTs
                    )
                    replyTo ! MessageBack(pong.asJson.toString)
                  }
                case Left(_) =>
                  replyTo ! MessageBack(StatusCodes.BadRequest.toString())
              }
              Behaviors.same
          }
        case _ => Behaviors.unhandled
      }
    }
}
