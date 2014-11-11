package dk.itu.spcl

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import dk.itu.spcl.server.PresenceServiceActor
import spray.can.Http

import scala.concurrent.duration._

object Infrastructure extends App {
  // ActorSystem which hosts our application
  implicit val system = ActorSystem("approximator-system")

  // Create and start our service actor
  val service = system.actorOf(Props[PresenceServiceActor], "server")

  // Set a default timeout for the start, this keeps us from receiving dead letters during startup
  implicit val timeout = Timeout(5.seconds)

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, Configuration.host, port = Configuration.portHttp)
}

object Configuration {
  import com.typesafe.config.ConfigFactory

  private val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  val host = config.getString("approximator.host")
  val portHttp = config.getInt("approximator.ports.http")
  val portWs   = config.getInt("approximator.ports.ws")
}
