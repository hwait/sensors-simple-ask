package sensorsapp.server

import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.typed.scaladsl.ActorSource
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import org.reactivestreams.Publisher

class PollRouter(pollServerActor: ActorRef[ServerMessage])(implicit system: ActorSystem[_]) {
  def websocketFlow: Flow[Message, Message, Any] = {
    val (actorRef: ActorRef[ServerMessage], publisher: Publisher[TextMessage.Strict]) =
      ActorSource
        .actorRef[ServerMessage](
          completionMatcher = { case Complete => },
          failureMatcher = { case Fail(ex) => ex },
          bufferSize = 8,
          overflowStrategy = OverflowStrategy.fail
        )
        .map { messageBack =>
          TextMessage.Strict(messageBack.toString)
        }
        .toMat(Sink.asPublisher(false))(Keep.both)
        .run()

    val sink: Sink[Message, Any] = Flow[Message]
      .map {
        case TextMessage.Strict(msg) =>
          pollServerActor ! MessageWithCallback(actorRef, msg)
        case _ =>
      }
      .to(Sink.ignore)

    Flow.fromSinkAndSource(sink, Source.fromPublisher(publisher))
  }

  val route: Route =
    (get & path("ws")) {
      handleWebSocketMessages(websocketFlow)
    }
}
