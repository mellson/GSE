package dk.itu.spcl.server

class AuthInfo(val user: AuthUser) {
  def hasPermission(path: String) = path match {
    case _ => true // Currently we allow access to all urls for every user
  } 
}