package dk.itu.spcl.server

import spray.routing.authentication.{UserPass, BasicAuth}
import spray.routing.directives.AuthMagnet
import scala.concurrent._

trait Authenticator {
  def basicUserAuthenticator(implicit ec: ExecutionContext): AuthMagnet[AuthInfo] = {
    def validateUser(optionalUserPass: Option[UserPass]): Option[AuthInfo] = {
      for {
        userPass <- optionalUserPass
        user <- Repository.hasUser(userPass.user)
        if user.passwordMatches(userPass.pass)
      } yield new AuthInfo(user)
    }

    def authenticator(userPass: Option[UserPass]): Future[Option[AuthInfo]] = Future { validateUser(userPass) }

    BasicAuth(authenticator _, realm = "Private API")
  }
}