package com.kyleu.projectile.services.socket

import com.kyleu.projectile.util.BinarySerializers.{Pickler, toByteArray}
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.util.Logging

class SocketConnection[Sent: Encoder: Pickler, Received: Decoder: Pickler](
    val key: String, val path: String, val binary: Boolean, val handler: EventHandler[Received]
) {
  protected[this] val socket = new NetworkSocket(handler)

  val url = websocketUrl(path)
  Logging.debug(s"Socket [$key] starting with url [$url]")
  socket.open(url)

  private[this] def websocketUrl(path: String) = {
    val loc = org.scalajs.dom.document.location
    val wsProtocol = if (loc.protocol == "https:") { "wss" } else { "ws" }
    val bin = if (binary) { "?binary=true" } else { "" }
    s"$wsProtocol://${loc.host}$path$bin"
  }

  def sendMessage(rm: Sent): Unit = if (socket.isConnected) {
    if (binary) {
      socket.sendBinary(toByteArray(boopickle.Default.Pickle.intoBytes(rm)))
    } else {
      socket.sendString(rm.asJson.spaces2)
    }
  } else {
    Logging.warn(s"Attempted send of message [${rm.getClass.getSimpleName}] when not connected")
  }
}
