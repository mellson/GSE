package dk.itu.spcl

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import dk.itu.spcl.server.{WebSocketActor, PresenceServiceActor, WebSocketActorServer}
import spray.can.Http

import scala.concurrent.duration._

object Infrastructure extends App {
  // ActorSystem which hosts our application
  implicit val system = ActorSystem("approximator-system")

  // Start the web socket actors
  private val rs = new WebSocketActorServer(Configuration.portWs)
  rs.forResource("/connect", Some(system.actorOf(Props[WebSocketActor], "connect")))
  rs.start()
  sys.addShutdownHook({system.shutdown();rs.stop()})

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
