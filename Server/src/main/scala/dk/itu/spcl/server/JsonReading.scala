package dk.itu.spcl.server


import org.joda.time.DateTime
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

case class JsonReading(name: String, time: String, value: String) {
  def date() = DateTime.parse(time)
}

object JsonReadingJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val jsonReadingFormat = jsonFormat3(JsonReading)
}