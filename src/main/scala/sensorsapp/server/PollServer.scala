package sensorsapp.server

import scala.concurrent.ExecutionContext
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import scala.util.{Success, Failure}

object PollServer {

  def startHttpServer(
      pollerverActor: ActorRef[ServerMessage]
  )(implicit system: ActorSystem[_]): Unit = {
    implicit val ec: ExecutionContext = system.executionContext
    val router = new PollRouter(pollerverActor)

    val httpBindingFuture = Http()
      .newServerAt(serverConfig.host, serverConfig.port)
      .bind(router.route)
    httpBindingFuture.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(
          s"Server online at http://${address.getHostString}:${address.getPort}"
        )
      case Failure(ex) =>
        system.log.error(s"Failed to bind HTTP server, because: $ex")
        system.terminate()
    }
  }
}
