package dk.itu.spcl.auth

object Repository {
  def hasUser(login: String): Option[AuthUser] = login match {
    // Only allow access for the user spcl
    case "spcl" => Some(AuthUser(login, Some("$2a$10$CcYNEoMm0K7W0pp609uS4Ob23u./PELNokCAm3wjQBB1l8w4Bn.LG")))
    case _      => None
  }
}
