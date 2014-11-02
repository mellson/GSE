package dk.itu.spcl.server

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import org.joda.time.{DateTime, Seconds}
import spray.json._
import spray.routing._
import DefaultJsonProtocol._
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

    case (_,Some(i))  => ok(s"Send updates to $sensorPath/$i")
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

  def getUserStatuses() = {
    implicit val timeout = Timeout(5 seconds)
    val timeToNonPresent = 30

    def getAvailability(secondsSinceLastReading: Int): Int = {
      val temp = 100 / (secondsSinceLastReading - timeToNonPresent)
      if (temp > 0) temp else 0
    }

    val userStatuses =
      for (user <- userIdMap) yield {
        val userName = user._1
        val userActor = user._2._2
        val future = userActor ? AskForLastUpdateMessage
        val lastReading = Await.result(future, timeout.duration).asInstanceOf[SensorReading]
        val secondsSinceLastUpdate = Seconds.secondsBetween(lastReading.date(), DateTime.now()).getSeconds
        val present = secondsSinceLastUpdate <= timeToNonPresent
        val availability = if (present) 100 else getAvailability(secondsSinceLastUpdate)
        UserStatus(userName, present, availability)
      }

    val jsonStatuses = userStatuses.map(u => Map(
      "UserName" -> u.userName,
      "Present" -> u.present.toString,
      "Availability" -> u.available.toString
    )).toList
    jsonStatuses.toJson
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
      path("users") {
        get {
          complete(getUserStatuses().prettyPrint)
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