package dk.itu.spcl.server

import org.joda.time.{DateTime, Seconds}
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.actor.ActorRef
import akka.util.Timeout
import akka.pattern.ask

case class Status(Present: Boolean, Availability: Int)

object DecisionModule {
  implicit val timeout = Timeout(5 seconds)
  val timeToNonPresent = 30

  def getAvailability(secondsSinceLastReading: Int): Int = {
    val temp = 100 / (secondsSinceLastReading - timeToNonPresent)
    if (temp > 0) temp else 0
  }

  def getPresenceAndAvailibility(actor: ActorRef): Status = {
    val future = actor ? AskForLastUpdateMessage
    val lastReading = Await.result(future, timeout.duration).asInstanceOf[SensorReading]
    val secondsSinceLastUpdate = Seconds.secondsBetween(lastReading.date(), DateTime.now()).getSeconds
    val present = secondsSinceLastUpdate <= timeToNonPresent
    val availability = if (present) 100 else getAvailability(secondsSinceLastUpdate)
    Status(present, availability)
  }
}
