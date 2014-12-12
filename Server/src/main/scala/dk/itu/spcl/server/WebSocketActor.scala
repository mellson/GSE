package dk.itu.spcl.server

import akka.actor.Actor
import dk.itu.spcl.approximator.{DecisionModule, SensorReadingWithTime, UserStatus}
import org.java_websocket.WebSocket
import org.joda.time.DateTime
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

import scala.collection._

object WebSocketActor {
  sealed trait FindMessage
  case object Clear extends FindMessage
  case class Unregister(ws : WebSocket) extends FindMessage
  case class Marker(id : String, idx : String) extends FindMessage
  case class Clear(marker : Marker) extends FindMessage
  case class Move(marker : Marker, longitude : String, latitude : String) extends FindMessage
}

class WebSocketActor extends Actor {
  import dk.itu.spcl.server.WebSocketActor._
  import dk.itu.spcl.server.WebSocketActorServer._

  type UserSocket = WebSocket
  val users = mutable.ListBuffer[UserSocket]()
  val markers = mutable.Map[Marker,Option[Move]]()

  def sendStatus(status: UserStatus) = {
    for (user <- users)
      user.send(status.toString)
  }

  var latestInterruptibility = 0
  override def receive = {
    case readings: List[SensorReadingWithTime] =>
      val interruptibility = DecisionModule.getInterruptibility(200, 30000, readings)
      if (interruptibility != latestInterruptibility) {
        latestInterruptibility = interruptibility
        println(s"Interruptibility $interruptibility")
        val userStatus = UserStatus(readings.head.UserName, interruptibility)
        val json =  ("User" -> userStatus.UserName) ~ ("Interruptibility" -> userStatus.Interruptibility)
        for (user <- users) user.send(compact(render(json)))
      }

    case Open(ws, hs) =>
      users += ws
      for (marker <- markers if None != marker._2) {
        ws.send(message(marker._2.get))
      }
      println(s"registered monitor for url ${ws.getResourceDescriptor}")

    case Close(ws, code, reason, ext) =>
      self ! Unregister(ws)

    case Error(ws, ex) =>
      self ! Unregister(ws)

    case Message(ws, msg) =>
      val time = DateTime.now.toString
      println(s"$time $msg")

    case Clear =>
      for (marker <- markers if None != marker._2) {
        val msg = message(marker._1)
        for (user <- users) {
          user.send(msg)
        }
      }
      markers.clear

    case Unregister(ws) =>
      if (null != ws) {
        println("unregister monitor " + ws)
        users -= ws
      }

    case Clear(marker) =>
      println("clear marker {} '{}'", marker.idx, marker.id)
      val msg = message(marker)
      markers -= marker
      for (user <- users) {
        user.send(msg)
      }
      println("sent to {} clients to clear marker '{}'", users.size, msg)

    case marker @ Marker(id, idx) =>
      markers += ((marker, None))
      println("create new marker {} '{}'", idx, id)

    case move @ Move(marker, lng, lat) =>
      markers += ((marker, Some(move)))
      val msg = message(move)
      for (user <- users) {
        user.send(msg)
      }
      println("sent to {} clients the new move '{}'", users.size, msg)
  }

  private def message(move :Move) = s"""{"move":{"id":"${move.marker.id}","idx":"${move.marker.idx}","longitude":${move.longitude},"latitude":${move.latitude}}}"""
  private def message(marker :Marker) = s"""{"clear":{"id":"${marker.id}","idx":"${marker.idx}"}}"""
}
