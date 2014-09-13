package dk.itu.spcl.server

import akka.actor.Actor
import spray.httpx.SprayJsonSupport
import spray.json._
import spray.routing._

// We don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class PresenceServiceActor extends Actor with PresenceService {
  def actorRefFactory = context

  def receive = runRoute(route)
}

case class Sensor(name: String, user: String)

object SensorJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val sensorFormat = jsonFormat2(Sensor)
}

// Defines our service behavior independently from the service actor
trait PresenceService extends HttpService with Authenticator {
  def registerSensor(sensor: Sensor) = {
    s"Registered sensor with name ${sensor.name}"
  }

  import dk.itu.spcl.server.SensorJsonSupport._
  import scala.concurrent.ExecutionContext.Implicits.global

  val route =
    path("") {
      get {
        complete("Hello from the presence service!")
      }
    } ~
      path("sensor") {
        post {
          entity(as[Sensor]) { sensor =>
            complete(registerSensor(sensor))
          }
        }
      } ~
      path("private") {
        authenticate(basicUserAuthenticator) { authInfo =>
        get {
          // All authenticated users can enter here
          complete(s"Hi, ${authInfo.user.login} you have access!")
        }
      }
    }
}