package dk.itu.spcl.server

import org.joda.time.DateTime

case class SensorReading(SensorName: String, Time: String, Value: String) {
  def date() = DateTime.parse(Time)
}