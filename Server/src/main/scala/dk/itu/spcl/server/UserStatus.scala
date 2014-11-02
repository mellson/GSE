package dk.itu.spcl.server

import spray.httpx.SprayJsonSupport
import spray.json._

case class UserStatus(userName: String, present: Boolean, available: Int)

object UserStatusJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val userStatusFormat = jsonFormat3(UserStatus)
}
