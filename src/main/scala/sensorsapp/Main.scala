package sensorsapp

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Success, Failure}
import akka.actor.{Actor, ActorLogging, Props}
import akka.actor.typed.{Scheduler, ActorRef, Behavior, ActorSystem}
import akka.stream.ActorMaterializer
import akka.pattern.ask
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import akka.NotUsed
import sensorsapp.actors.PollActor
import sensorsapp.domain.Command
import sensorsapp.server.{ServerMessage, PollServerActor, PollServer}

object Main {
  def main(args: Array[String]): Unit = {

    trait RootCommand
    case class RetrievePollActor(replyTo: ActorRef[ActorRef[Command]])
        extends RootCommand
    case class RetrieveServerActor(replyTo: ActorRef[ActorRef[ServerMessage]])
        extends RootCommand

    val rootBehavior: Behavior[RootCommand] = Behaviors.setup { context =>
      val pollActor = context.spawn(PollActor(), "pollActor")
      val serverActor = context.spawn(PollServerActor(pollActor), "serverActor")
      val logger = context.log
      Behaviors.receiveMessage {
        case RetrievePollActor(replyTo) =>
          replyTo ! pollActor
          Behaviors.same
        case RetrieveServerActor(replyTo) =>
          replyTo ! serverActor
          Behaviors.same
      }
    }

    implicit val system: ActorSystem[RootCommand] =
      ActorSystem(rootBehavior, "pollSystem")
    implicit val timeout: Timeout = Timeout(5.seconds)
    implicit val ec: ExecutionContext = system.executionContext

    val serverActorFuture: Future[ActorRef[ServerMessage]] =
      system.ask(replyTo => RetrieveServerActor(replyTo))
    serverActorFuture.foreach(PollServer.startHttpServer)
  }

}
