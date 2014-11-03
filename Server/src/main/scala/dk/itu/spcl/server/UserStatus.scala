package dk.itu.spcl.server

import spray.httpx.SprayJsonSupport
import spray.json._

case class UserStatus(UserName: String, Present: Boolean, Available: Int)

object UserStatusJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val userStatusFormat = jsonFormat3(UserStatus)
}
