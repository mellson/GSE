package dk.itu.spcl.approximator

import akka.actor.Actor
import dk.itu.spcl.Infrastructure
import dk.itu.spcl.server.{AskForLastUpdateMessage, GetReadings}
import org.joda.time.DateTime

class UserActor extends Actor {
  var lastReading: SensorReadingWithTime = SensorReadingWithTime("DefaultSensor", "DefaultUser", DateTime.now.toString, "Init")
  var readings: List[SensorReadingWithTime] = Nil

  override def receive: Receive = {
    case sensorReading: SensorReading =>
      val reading = SensorReadingWithTime(sensorReading.SensorName, sensorReading.UserName, DateTime.now.toString, sensorReading.Value)

      println(reading)

      lastReading = reading

      // Keep all reading from the last 30 seconds
      readings = reading :: readings.filter(time => time.date().minusSeconds(30).isBeforeNow)

      // Update all clients connected via web socket with this latest sensor reading
      Infrastructure.webSocketActor ! readings

    case AskForLastUpdateMessage => sender ! lastReading

    case GetReadings => sender ! readings
  }
}
