package dk.itu.spcl.server

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import dk.itu.spcl.auth.Authenticator
import dk.itu.spcl.json.CustomJsonFormats
import dk.itu.spcl.approximator._
import spray.routing._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import CustomJsonFormats._

class RestActor extends RestService with Actor {
  def receive = runRoute(route)
}

// Defines our service behavior independently from the service actor
trait RestService extends HttpServiceActor with Authenticator with akka.actor.ActorLogging {
  def ok(s: String) = Map("Ok" -> s)
  def error(s: String) = Map("Error" -> s)

  var users:Map[String, (Int, ActorRef)] = Map.empty
  var idCounter = 0
  val userPath = "user"

  def getPostStringFromUserId(sensorRegistration: SensorRegistration) = (sensorRegistration.UserName, users.get(sensorRegistration.UserName)) match {
    case ("", None) => error("You need to provide a user id")
    case (_, None)  =>
      val id = idCounter
      val userActor = actorRefFactory.actorOf(Props[UserActor], sensorRegistration.UserName)
      users += (sensorRegistration.UserName -> (id, userActor))
      idCounter += 1
      ok(s"$userPath/$id")
    case (_,Some(i: (Int, ActorRef)))  => ok(s"$userPath/${i._1}")
  }

  def getSensorData(id: Int): List[SensorReadingWithTime] = {
    implicit val timeout = Timeout(5 seconds)
    for (user <- users)
      if (user._2._1 == id) {
        val userActor = user._2._2
        val future = userActor ? GetReadings
        val readings = Await.result(future, timeout.duration).asInstanceOf[List[SensorReadingWithTime]]
        return readings
      }
    Nil
  }

  def putSensorData(id: Int, sensorReading: SensorReading) = {
    val user = users.get(sensorReading.UserName)
    if (!user.isDefined)
      error("No user registered for this sensor")
    else if (user.isDefined && user.get._1 != id)
      error("Wrong id")
    else {
      user.get._2 ! sensorReading
      ok(s"Received sensor data")
    }
  }

  def getUserStatuses = {
    val reading = SensorReading("userPath", "userName", "value")
    log.debug(reading.toString)
    println(reading) // TODO remove this again

    val userStatuses =
      for (user <- users) yield {
        val userName = user._1
        val userActor = user._2._2
        val status = DecisionModule.getPresenceAndAvailability(userActor)
        UserStatus(userName, status.Present, status.Availability)
      }
    userStatuses.toList
  }

  val route =
    path("") {
      get {
        complete("Hello from the presence service!")
      }
    } ~
      path("register") {
        put {
          entity(as[SensorRegistration]) { sensorRegistration =>
            complete(getPostStringFromUserId(sensorRegistration))
          }
        }
      } ~
      path(userPath / IntNumber) { id =>
      {
        put {
          entity(as[SensorReading]) { sensorReading =>
            complete(putSensorData(id, sensorReading))
          }
        }
      }
      } ~
      path(userPath / IntNumber) { id =>
      {
        get {
          complete(getSensorData(id))
        }
      }
      } ~
      path("users") {
        get {
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