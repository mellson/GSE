package dk.itu.spcl.approximator

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import dk.itu.spcl.server.AskForLastUpdateMessage
import org.joda.time.{DateTime, Seconds}
import scala.concurrent.Await
import scala.concurrent.duration._

case class Status(Present: Boolean, Availability: Int)

object DecisionModule {
  implicit val timeout = Timeout(5 seconds)
  val timeToNonPresent = 30

  // Degrade availability with 1 point every second after timeToNonPresent
  def getAvailability(secondsSinceLastReading: Int): Int = {
    val temp = 100 - (secondsSinceLastReading - timeToNonPresent)
    if (temp > 0 && temp < 100) temp else if (temp > 100) 100 else 0
  }
  
  def getPresence(secondsSinceLastReading: Int): Boolean = secondsSinceLastReading < timeToNonPresent

  def getPresenceAndAvailability(actor: ActorRef): Status = {
    val future = actor ? AskForLastUpdateMessage
    val lastReading = Await.result(future, timeout.duration).asInstanceOf[SensorReadingWithTime]
    val secondsSinceLastUpdate = Seconds.secondsBetween(lastReading.date(), DateTime.now()).getSeconds
    val present = getPresence(secondsSinceLastUpdate)
    val availability = getAvailability(secondsSinceLastUpdate)
    Status(present, availability)
  }
}
