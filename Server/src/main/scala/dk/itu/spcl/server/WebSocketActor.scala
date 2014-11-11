package dk.itu.spcl.server

import akka.actor.Actor
import dk.itu.spcl.approximator.{DecisionModule, SensorReadingWithTime, UserStatus}
import org.java_websocket.WebSocket
import org.joda.time.{DateTime, Seconds}
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

  val clients = mutable.ListBuffer[WebSocket]()
  val markers = mutable.Map[Marker,Option[Move]]()

  def sendStatus(status: UserStatus) = {
    for (client <- clients)
      client.send(status.toString)
  }

  override def receive = {
    // Update all clients connected via web socket with the latest sensor reading
    case lastReading: SensorReadingWithTime =>
      val secondsSinceLastUpdate = Seconds.secondsBetween(lastReading.date(), DateTime.now()).getSeconds
      val present = DecisionModule.getPresence(secondsSinceLastUpdate)
      val availability = DecisionModule.getAvailability(secondsSinceLastUpdate)
      val userStatus = UserStatus(lastReading.UserName, present, availability)
      val json =
        ("User" -> userStatus.UserName) ~ ("Present" -> userStatus.Present) ~ ("Availability" -> userStatus.Available)
      for (client <- clients)
        client.send(compact(render(json)))

    case Open(ws, hs) => {
      clients += ws
      for (marker <- markers if None != marker._2) {
        ws.send(message(marker._2.get))
      }
      println(s"registered monitor for url ${ws.getResourceDescriptor}")
    }

    case Close(ws, code, reason, ext) =>
      self ! Unregister(ws)

    case Error(ws, ex) =>
      self ! Unregister(ws)

    case Message(ws, msg) =>
      println(s"url ${ws.getResourceDescriptor} received msg '$msg'")

    case Clear => {
      for (marker <- markers if None != marker._2) {
        val msg = message(marker._1)
        for (client <- clients) {
          client.send(msg)
        }
      }
      markers.clear
    }

    case Unregister(ws) => {
      if (null != ws) {
        println("unregister monitor " + ws)
        clients -= ws
      }
    }

    case Clear(marker) => {
      println("clear marker {} '{}'", marker.idx, marker.id)
      val msg = message(marker)
      markers -= marker
      for (client <- clients) {
        client.send(msg)
      }
      println("sent to {} clients to clear marker '{}'", clients.size, msg)
    }

    case marker @ Marker(id, idx) => {
      markers += ((marker, None))
      println("create new marker {} '{}'", idx, id)
    }
    case move @ Move(marker, lng, lat) => {
      markers += ((marker, Some(move)))
      val msg = message(move)
      for (client <- clients) {
        client.send(msg)
      }
      println("sent to {} clients the new move '{}'", clients.size, msg)
    }
  }

  private def message(move :Move) = s"""{"move":{"id":"${move.marker.id}","idx":"${move.marker.idx}","longitude":${move.longitude},"latitude":${move.latitude}}}"""
  private def message(marker :Marker) = s"""{"clear":{"id":"${marker.id}","idx":"${marker.idx}"}}"""
}
