package dk.itu.spcl.server

import akka.actor.Actor
import spray.http.DateTime

class SensorCluster extends Actor {
  var lastReading: SensorReading = SensorReading("DefaultName", DateTime.now.toString(), "Init")
  var readings: List[SensorReading] = Nil

  override def receive: Receive = {
    case sensorReading: SensorReading =>
      val reading = SensorReading(sensorReading.SensorName, DateTime.now.toString(), sensorReading.Value)
      lastReading = reading
      if (readings.length < 10)
        readings = reading :: readings
      else
        readings = reading :: readings.take(9)
    case AskForLastUpdateMessage => sender ! lastReading
    case GetReadings => sender ! readings
  }
}
