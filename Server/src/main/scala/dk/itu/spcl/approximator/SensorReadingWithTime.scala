package dk.itu.spcl.approximator

import org.joda.time.DateTime

case class SensorRegistration(SensorName: String, UserName: String)

case class SensorReading(SensorName: String, UserName: String, Value: String)

case class SensorReadingWithTime(SensorName: String, UserName: String, Time: String, Value: String) {
  def date() = DateTime.parse(Time)
}