package dk.itu.spcl.approximator

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import dk.itu.spcl.server.{AskForLastUpdateMessage, GetReadings}
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
  
  def mouseReading(reading: SensorReadingWithTime) = reading.SensorName.contains("MouseSensor")
  def keyboardReading(reading: SensorReadingWithTime) = reading.SensorName.contains("KeyboardSensor")
  def faceDetectionReading(reading: SensorReadingWithTime) = reading.SensorName.contains("FaceDetectionSensor") && reading.Value.contains("Present with")

  def faceDetectionValue(readings: List[SensorReadingWithTime]): Int =  {
    readings.reverse.take(5).length
  }

  def scenario1(readings: List[SensorReadingWithTime], maxReadings: Int): Boolean = {
    val t1 = readings.length < (maxReadings / 5)
    val t2 = readings count faceDetectionReading
    t1 && t2 > 0
  }
  
  def scenario2(readings: List[SensorReadingWithTime]): Boolean = {
    val mouseReadings = readings count mouseReading
    val keyboardReadings = readings count keyboardReading
    val faceDetectionReadings = readings count faceDetectionReading
    keyboardReadings > mouseReadings / 10 && keyboardReadings > faceDetectionReadings
  }

  def mouseActivity(mouseReadings: Int, maxReadings: Int): Int = {
    val veryLowMouseReading = 5
    def mouseActivityHelper(tempReading: Int): Int = tempReading <= veryLowMouseReading match {
      case true  => 0
      case false => 1 + mouseActivityHelper(tempReading - maxReadings / 5)
    }
    mouseActivityHelper(mouseReadings)
  }

  var scenarioHelper = ""
  def getInterruptibility(samplingFreqMs: Int, samplingWindowMs: Int, readings: List[SensorReadingWithTime]): Int = {
    var interruptibility = 1

    // Scenario 1 => Reading / skyping etc
    val maxReadings = samplingWindowMs / samplingFreqMs
    if (scenario1(readings, maxReadings)) {
      if (scenarioHelper != "scenario 1") {
        scenarioHelper = "scenario 1"
        println(scenarioHelper)
      }
      interruptibility = 5
    }

    // Scenario 2 => User is coding / writing etc
    if (scenario2(readings)) {
      if (scenarioHelper != "scenario 2") {
        scenarioHelper = "scenario 2"
        println(scenarioHelper)
      }
      interruptibility = 5
    }

    if (interruptibility > 1)
      interruptibility = interruptibility - mouseActivity(readings count mouseReading, maxReadings)

    interruptibility
  }

  def getInterruptibility(actor: ActorRef): Int = {
    val future = actor ? GetReadings
    val readings = Await.result(future, timeout.duration).asInstanceOf[List[SensorReadingWithTime]]
    getInterruptibility(200, 30000, readings)
  }
}
