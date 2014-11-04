package dk.itu.spcl.server

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

object Server extends App {
  // ActorSystem which hosts our application
  implicit val system = ActorSystem("spray-can")

  // Create and start our service actor
  val service = system.actorOf(Props[PresenceServiceActor], "server")

  // Set a default timeout for the start, this keeps us from receiving dead letters during startup
  implicit val timeout = Timeout(5.seconds)

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, "0.0.0.0", port = 8080)
}
