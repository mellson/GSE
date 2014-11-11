package dk.itu.spcl.server

import java.net.InetSocketAddress

import akka.actor.ActorRef
import org.java_websocket.WebSocket
import org.java_websocket.framing.CloseFrame
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer

import scala.collection.mutable.Map


object WebSocketActorServer {
  sealed trait WebSocketActorServerMessage
  case class Message(ws : WebSocket, msg : String)
    extends WebSocketActorServerMessage
  case class Open(ws : WebSocket, hs : ClientHandshake)
    extends WebSocketActorServerMessage
  case class Close(ws : WebSocket, code : Int, reason : String, external : Boolean)
    extends WebSocketActorServerMessage
  case class Error(ws : WebSocket, ex : Exception)
    extends WebSocketActorServerMessage
}
class WebSocketActorServer(val port : Int)
  extends WebSocketServer(new InetSocketAddress(port)) {
  private val webSocketActors = Map[String, ActorRef]()

  final def forResource(descriptor : String, reactor : Option[ActorRef]) {
    reactor match {
      case Some(actor) => webSocketActors += ((descriptor, actor))
      case None => webSocketActors -= descriptor
    }
  }

  final override def onMessage(ws : WebSocket, msg : String) {
    if (null != ws) {
      webSocketActors.get(ws.getResourceDescriptor) match {
        case Some(actor) => actor ! WebSocketActorServer.Message(ws, msg)
        case None => ws.close(CloseFrame.REFUSE)
      }
    }
  }

  final override def onOpen(ws : WebSocket, hs : ClientHandshake) {
    if (null != ws) {
      webSocketActors.get(ws.getResourceDescriptor) match {
        case Some(actor) => actor ! WebSocketActorServer.Open(ws, hs)
        case None => ws.close(CloseFrame.REFUSE)
      }
    }
  }

  final override def onClose(ws : WebSocket, code : Int, reason : String, external : Boolean) {
    if (null != ws) {
      webSocketActors.get(ws.getResourceDescriptor) match {
        case Some(actor) => actor ! WebSocketActorServer.Close(ws, code, reason, external)
        case None => ws.close(CloseFrame.REFUSE)
      }
    }
  }

  final override def onError(ws : WebSocket, ex : Exception) {
    if (null != ws) {
      webSocketActors.get(ws.getResourceDescriptor) match {
        case Some(actor) => actor ! WebSocketActorServer.Error(ws, ex)
        case None => ws.close(CloseFrame.REFUSE)
      }
    }
  }
}
