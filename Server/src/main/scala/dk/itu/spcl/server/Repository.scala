package dk.itu.spcl.server

object Repository {
  def hasUser(login: String): Option[User] = login match {
    // Only allow access for the user spcl
    case "spcl" => Some(User(login, Some("$2a$10$CcYNEoMm0K7W0pp609uS4Ob23u./PELNokCAm3wjQBB1l8w4Bn.LG")))
    case _      => None
  }
}
