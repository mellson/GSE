package dk.itu.spcl.server

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

case class Sensor(name: String, user: String, jsonData: String)
object SensorJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val sensorFormat = jsonFormat3(Sensor)
}