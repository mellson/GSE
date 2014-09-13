package dk.itu.spcl.server

import com.github.t3hnar.bcrypt._
import org.mindrot.jbcrypt.BCrypt

case class User(login: String, hashedPassword: Option[String] = None) {
  def withPassword(password: String) = copy (hashedPassword = Some(password.bcrypt(generateSalt)))
  def passwordMatches(password: String): Boolean =  hashedPassword.exists(hp => BCrypt.checkpw(password, hp))
}