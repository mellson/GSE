package dk.itu.spcl.server

import akka.actor.Actor

class SensorCluster extends Actor {
  override def receive: Receive = {
    case sensorData: Sensor => println(s"$sensorData")
  }
}
