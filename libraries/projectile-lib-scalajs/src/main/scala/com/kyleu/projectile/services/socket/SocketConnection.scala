package com.kyleu.projectile.services.socket

import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.util.Logging

class SocketConnection[Sent: Encoder, Received: Decoder](val key: String, val path: String, val handler: EventHandler[Received]) {
  protected[this] val socket = new NetworkSocket(handler)

  connect(path)

  def connect(path: String) = {
    val url = websocketUrl(path)
    Logging.debug(s"Socket [$key] starting with url [$url].")
    socket.open(url)
  }

  private[this] def websocketUrl(path: String) = {
    val loc = org.scalajs.dom.document.location
    val wsProtocol = if (loc.protocol == "https:") { "wss" } else { "ws" }
    s"$wsProtocol://${loc.host}$path"
  }

  def sendMessage(rm: Sent): Unit = if (socket.isConnected) {
    socket.sendString(rm.asJson.spaces2)
  } else {
    Logging.warn(s"Attempted send of message [${rm.getClass.getSimpleName}] when not connected.")
  }
}
