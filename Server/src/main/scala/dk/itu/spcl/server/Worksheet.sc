import org.joda.time.DateTime

val t = "2014-11-01T16:26:20.8422459+01:00"
val d = DateTime.parse(t)
d.dayOfWeek().get()
d.hourOfDay().get()
d.minuteOfDay().get()

import spray.json._
import DefaultJsonProtocol._
val json = """{"SensorName":"MouseSensor","Time":"2014-11-01T16:26:04.8345115+01:00","Value":"X:1011,Y:419"}"""
for (o <- JsonParser(json))
  println(1)

