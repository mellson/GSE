package dk.itu.spcl.json

import dk.itu.spcl.approximator.{SensorRegistration, SensorReadingWithTime, SensorReading, UserStatus}
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

// Provide a conversion to Json for all custom objects here
object CustomJsonFormats extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val userStatusFormat = jsonFormat2(UserStatus)
  implicit val sensorReadingFormat = jsonFormat3(SensorReading)
  implicit val sensorReadingWithTimeFormat = jsonFormat4(SensorReadingWithTime)
  implicit val sensorRegistrationFormat = jsonFormat2(SensorRegistration)
}