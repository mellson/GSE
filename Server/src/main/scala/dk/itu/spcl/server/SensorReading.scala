package dk.itu.spcl.server

import org.joda.time.DateTime
import spray.httpx.SprayJsonSupport
import spray.json._

case class SensorReading(SensorName: String, Time: String, Value: String) {
  def date() = DateTime.parse(Time)
}
object SensorReadingJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val sensorReadingFormat = jsonFormat3(SensorReading)
}