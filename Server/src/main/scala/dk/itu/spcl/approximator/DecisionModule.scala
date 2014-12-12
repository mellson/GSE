package dk.itu.spcl.approximator

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import dk.itu.spcl.Infrastructure
import dk.itu.spcl.server.{GetReadings, AskForLastUpdateMessage}
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
    Infrastructure.log.error("hej") // TODO remove

    val future = actor ? AskForLastUpdateMessage
    val lastReading = Await.result(future, timeout.duration).asInstanceOf[SensorReadingWithTime]
    val secondsSinceLastUpdate = Seconds.secondsBetween(lastReading.date(), DateTime.now()).getSeconds
    val present = getPresence(secondsSinceLastUpdate)
    val availability = getAvailability(secondsSinceLastUpdate)
    Status(present, availability)
  }

  def getInterruptibility(readings: List[SensorReadingWithTime]): Int = {
    val mouseReadings = readings.filter(r => r.SensorName.contains("MouseSensor"))
    val keyboardReadings = readings.filter(r => r.SensorName.contains("KeyboardSensor"))
    val facedetectionReadings = readings.filter(r => r.SensorName.contains("FaceDetectionSensor") && r.Value.contains("Present with"))

    // How many readings within the last 30 seconds to be high intensity
    val highIntensityRate = 50

    // maximum interruptibility
    val maximumInterruptibility = 5

    val mousePercentage = mouseReadings.length / (readings.length * 1.0)
    val keyboardPercentage = keyboardReadings.length / (readings.length * 1.0)
    val facedetectionPercentage = facedetectionReadings.length / (readings.length * 1.0)

    val keyValue = keyboardPercentage * maximumInterruptibility
    val faceValue = facedetectionPercentage * maximumInterruptibility
    val combinedValue = keyValue + faceValue



    if (combinedValue < 1)
      1
    else
      combinedValue.ceil.toInt
  }

  def getInterruptibility(actor: ActorRef): Int = {
    val future = actor ? GetReadings
    val readings = Await.result(future, timeout.duration).asInstanceOf[List[SensorReadingWithTime]]
    getInterruptibility(readings)
  }
}
