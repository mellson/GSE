package dk.itu.spcl.server

import akka.actor.Actor

class SensorCluster extends Actor {
  override def receive: Receive = {
    case sensor: Sensor =>
      val jsonReading = sensor.jsonData
      println(s"$sensor")
  }
}
