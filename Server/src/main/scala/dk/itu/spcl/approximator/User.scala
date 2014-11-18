package dk.itu.spcl.approximator

import akka.actor.Actor
import dk.itu.spcl.Infrastructure
import dk.itu.spcl.server.{AskForLastUpdateMessage, GetReadings}
import org.joda.time.DateTime

class User extends Actor {
  var lastReading: SensorReadingWithTime = SensorReadingWithTime("DefaultSensor", "DefaultUser", DateTime.now.toString, "Init")
  var readings: List[SensorReadingWithTime] = Nil

  override def receive: Receive = {
    case sensorReading: SensorReading =>
      val reading = SensorReadingWithTime(sensorReading.SensorName, sensorReading.UserName, DateTime.now.toString, sensorReading.Value)

      // Update all clients connected via web socket with this latest sensor reading
      Infrastructure.webSocketActor ! reading

      lastReading = reading
      if (readings.length < 10)
        readings = reading :: readings
      else
        readings = reading :: readings.take(9)

    case AskForLastUpdateMessage => sender ! lastReading

    case GetReadings => sender ! readings
  }
}
