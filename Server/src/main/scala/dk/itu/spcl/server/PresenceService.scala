package dk.itu.spcl.server

import akka.actor.Actor
import spray.httpx.SprayJsonSupport
import spray.json._
import spray.routing._

class PresenceServiceActor extends PresenceService with Actor {
  def receive = runRoute(route)
}

case class Sensor(name: String, user: String)
object SensorJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val sensorFormat = jsonFormat2(Sensor)
}

// Defines our service behavior independently from the service actor
trait PresenceService extends HttpServiceActor with Authenticator {
  import dk.itu.spcl.server.SensorJsonSupport._
  import scala.concurrent.ExecutionContext.Implicits.global

  def ok(s: String) = Map("Ok" -> s)
  def error(s: String) = Map("Error" -> s)

  var userIdMap:Map[String, Int] = Map.empty
  var idCounter = 0

  val sensorPath = "sensor"

  def getPostStringFromUserId(sensor: Sensor) = (sensor.user, userIdMap.get(sensor.user)) match {
    case ("", None) => error("You need to provide a user id")

    case (_, None)  =>
      val id = idCounter
      userIdMap += (sensor.user -> id)
      idCounter += 1
      ok(s"Send updates to $sensorPath/$id")

    case (_,Some(i))  => ok(s"Send updates to $sensorPath/$i")
  }

  def registerSensor(id: Int, sensor: Sensor) = {
    val user = userIdMap.get(sensor.user)
    if (!user.isDefined)
      error("No user registered for this sensor")
    else if (user.isDefined && user.get != id)
      error("Wrong id")
    else
      ok(s"Registered sensor with name ${sensor.name}")
  }
  val route =
    path("") {
      get {
        complete("Hello from the presence service!")
      }
    } ~
      path("register") {
        put {
          entity(as[Sensor]) { sensor =>
            complete(getPostStringFromUserId(sensor))
          }
        }
      } ~
      path(sensorPath / IntNumber) { id =>
      {
        put {
          entity(as[Sensor]) { sensor =>
            complete(registerSensor(id, sensor))
          }
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