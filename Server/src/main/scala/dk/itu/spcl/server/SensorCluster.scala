package dk.itu.spcl.server

import akka.actor.Actor
import spray.http.DateTime

class SensorCluster extends Actor {
  var lastReading: SensorReading = SensorReading("DefaultName", DateTime.now.toString(), "Init")

  override def receive: Receive = {
    case sensor: SensorReading => lastReading = sensor
    case AskForLastUpdateMessage => sender ! lastReading
  }
}
