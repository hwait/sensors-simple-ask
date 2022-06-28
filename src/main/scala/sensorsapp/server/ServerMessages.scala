package sensorsapp.server

//import cats.syntax.functor._
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser.decode
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import akka.actor.typed.ActorRef
import sensorsapp.domain.Sensor
import sensorsapp.domain.Commands.PollResult

import Validation._

sealed trait RequestMessage
case class PingRequest(message_type: String, id: Int, ts: Long)
    extends RequestMessage
object PingRequest {
  implicit val validator: Validator[PingRequest] = new Validator[PingRequest] {
    override def validate(req: PingRequest): ValidationResult[PingRequest] = {
      val messageTypeValidation =
        validateExact(req.message_type, "ping", "message_type")
      val idValidator = validateMinimum(req.id, 0, "id")
      val timestampValidator = validateMinimum(req.ts, 0, "ts")
      (messageTypeValidation, idValidator, timestampValidator).mapN(
        PingRequest.apply
      )
    }
  }
}

case class PongResponse(
    message_type: String,
    request_id: Int,
    request_at: Long,
    ts: Long
) extends RequestMessage

case class PollRequest(message_type: String, sensors: Int)
    extends RequestMessage
object PollRequest {
  implicit val validator: Validator[PollRequest] = new Validator[PollRequest] {
    override def validate(req: PollRequest): ValidationResult[PollRequest] = {
      val messageTypeValidation =
        validateExact(req.message_type, "poll", "message_type")
      val sensorsValidator = validateMinimum(
        req.sensors,
        1,
        "sensors"
      ) 
      (messageTypeValidation, sensorsValidator).mapN(PollRequest.apply)
    }
  }
}

case class PollResponse(message_type: String, sensors: List[Sensor])
    extends RequestMessage
object PollResponse {
  def apply(pollResult: PollResult): PollResponse =
    PollResponse("result", pollResult.sensors)
}

sealed trait ServerMessage
case class MessageWithCallback(
    replyTo: ActorRef[ServerMessage],
    message: String
) extends ServerMessage
case class MessageBack(message: String) extends ServerMessage {
  override def toString(): String = message
}
case object Complete extends ServerMessage
case class Fail(ex: Exception) extends ServerMessage


object GenericDerivation {
  implicit val encodeRequestMessage: Encoder[RequestMessage] =
    Encoder.instance {
      case poll @ PollRequest(_, _)    => poll.asJson
      case ping @ PingRequest(_, _, _) => ping.asJson
    }

  implicit val decodeRequestMessage: Decoder[RequestMessage] =
    List[Decoder[RequestMessage]](
      Decoder[PollRequest].widen,
      Decoder[PingRequest].widen
    ).reduceLeft(_ or _)
}
