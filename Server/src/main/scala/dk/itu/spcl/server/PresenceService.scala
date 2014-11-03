package dk.itu.spcl.server

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import spray.routing._
import scala.concurrent.Await
import scala.concurrent.duration._

class PresenceServiceActor extends PresenceService with Actor {
  def receive = runRoute(route)
}

// Defines our service behavior independently from the service actor
trait PresenceService extends HttpServiceActor with Authenticator {
  import scala.concurrent.ExecutionContext.Implicits.global

  def ok(s: String) = Map("Ok" -> s)
  def error(s: String) = Map("Error" -> s)

  var userIdMap:Map[String, (Int, ActorRef)] = Map.empty
  var idCounter = 0
  val sensorPath = "sensor"

  def getPostStringFromUserId(sensor: SensorReading) = (sensor.SensorName, userIdMap.get(sensor.SensorName)) match {
    case ("", None) => error("You need to provide a user id")
    case (_, None)  =>
      val id = idCounter
      val sensorCluster = actorRefFactory.actorOf(Props[SensorCluster], sensor.SensorName)
      userIdMap += (sensor.SensorName -> (id, sensorCluster))
      idCounter += 1
      ok(s"Send updates to $sensorPath/$id")
    case (_,Some(i: (Int, ActorRef)))  => ok(s"Send updates to $sensorPath/${i._1}")
  }

  def getSensorData(id: Int): List[SensorReading] = {
    implicit val timeout = Timeout(5 seconds)
    for (user <- userIdMap)
      if (user._2._1 == id) {
        val userActor = user._2._2
        val future = userActor ? GetReadings
        val readings = Await.result(future, timeout.duration).asInstanceOf[List[SensorReading]]
        return readings
      }
    Nil
  }

  def getSensorData(id: Int, sensor: SensorReading) = {
    val user = userIdMap.get(sensor.SensorName)
    if (!user.isDefined)
      error("No user registered for this sensor")
    else if (user.isDefined && user.get._1 != id)
      error("Wrong id")
    else {
      user.get._2 ! sensor
      ok(s"Received sensor data")
    }
  }

  def getUserStatuses = {
    val userStatuses =
      for (user <- userIdMap) yield {
        val userName = user._1
        val userActor = user._2._2
        val status = DecisionModule.getPresenceAndAvailability(userActor)
        UserStatus(userName, status.Present, status.Availability)
      }
    userStatuses.toList
  }


  import dk.itu.spcl.server.SensorReadingJsonSupport._
  val route =
    path("") {
      get {
        complete("Hello from the presence service!")
      }
    } ~
      path("register") {
        put {
          entity(as[SensorReading]) { sensorReading =>
            complete(getPostStringFromUserId(sensorReading))
          }
        }
      } ~
      path(sensorPath / IntNumber) { id =>
      {
        put {
          entity(as[SensorReading]) { sensorReading =>
            complete(getSensorData(id, sensorReading))
          }
        }
      }
      } ~
      path(sensorPath / IntNumber) { id =>
      {
        get {
          complete(getSensorData(id))
        }
      }
      } ~
      path("users") {
        get {
          import dk.itu.spcl.server.UserStatusJsonSupport._
          complete(getUserStatuses)
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