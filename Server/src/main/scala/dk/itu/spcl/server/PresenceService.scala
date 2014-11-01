package dk.itu.spcl.server

import akka.actor.{Props, ActorRef, Actor}
import spray.routing._

class PresenceServiceActor extends PresenceService with Actor {
  def receive = runRoute(route)
}

// Defines our service behavior independently from the service actor
trait PresenceService extends HttpServiceActor with Authenticator {
  import dk.itu.spcl.server.SensorJsonSupport._
  import scala.concurrent.ExecutionContext.Implicits.global

  def ok(s: String) = Map("Ok" -> s)
  def error(s: String) = Map("Error" -> s)

  var userIdMap:Map[String, (Int, ActorRef)] = Map.empty
  var idCounter = 0

  val sensorPath = "sensor"

  def getPostStringFromUserId(sensor: Sensor) = (sensor.user, userIdMap.get(sensor.user)) match {
    case ("", None) => error("You need to provide a user id")

    case (_, None)  =>
      val id = idCounter
      val sensorCluster = actorRefFactory.actorOf(Props[SensorCluster], sensor.user)
      userIdMap += (sensor.user -> (id, sensorCluster))
      idCounter += 1
      ok(s"Send updates to $sensorPath/$id")

    case (_,Some(i))  => ok(s"Send updates to $sensorPath/$i")
  }

  def getSensorData(id: Int, sensor: Sensor) = {
    val user = userIdMap.get(sensor.user)
    if (!user.isDefined)
      error("No user registered for this sensor")
    else if (user.isDefined && user.get._1 != id)
      error("Wrong id")
    else {
      user.get._2 ! sensor
      ok(s"Received sensor data")
    }
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
            complete(getSensorData(id, sensor))
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