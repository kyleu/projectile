package com.kyleu.projectile.services.socket

import java.nio.ByteBuffer

import com.kyleu.projectile.util.BinarySerializers.Pickler
import com.kyleu.projectile.util.{ArrayBufferOps, BinarySerializers}
import com.kyleu.projectile.util.JsonSerializers.Decoder
import com.kyleu.projectile.util.JsonSerializers.decodeJson
import org.scalajs.dom.raw._

import scala.scalajs.js.typedarray.{ArrayBuffer, TypedArrayBuffer}

class NetworkSocket[T: Decoder: Pickler](handler: EventHandler[T]) {
  private[this] var connecting = false
  private[this] var connected = false

  private[this] var ws: Option[WebSocket] = None

  private[this] var sentMessageCount = 0
  private[this] var sentBytes = 0

  private[this] var receivedMessageCount = 0
  private[this] var receivedBytes = 0

  def open(url: String) = if (connected) {
    throw new IllegalStateException("Already connected")
  } else if (connecting) {
    throw new IllegalStateException("Already connecting")
  } else {
    openSocket(url)
  }

  def sendString(s: String): Unit = {
    val socket = ws.getOrElse(throw new IllegalStateException("No available socket connection"))
    sentMessageCount += 1
    sentBytes += s.getBytes.length
    socket.send(s)
  }

  def sendBinary(data: Array[Byte]): Unit = {
    val socket = ws.getOrElse(throw new IllegalStateException("No available socket connection"))
    sentMessageCount += 1
    sentBytes += data.length
    socket.send(ArrayBufferOps.convertBuffer(ByteBuffer.wrap(data)))
  }

  def isConnected = connected

  private[this] def openSocket(url: String) = {
    connecting = true
    val socket = new WebSocket(url)
    socket.onopen = { event: Event => onConnectEvent(event) }
    socket.onerror = { event: Event => onErrorEvent(event) }
    socket.onmessage = { event: MessageEvent => onMessageEvent(event) }
    socket.onclose = { event: Event => onCloseEvent(event) }
    ws = Some(socket)
  }

  private[this] def onConnectEvent(event: Event) = {
    connecting = false
    connected = true
    handler.onConnect()
    event
  }

  private[this] def onErrorEvent(event: Event) = {
    handler.onError(event)
    event
  }

  private[this] def process(msg: T) = {
    receivedMessageCount += 1
    handler.onMessage(msg)
  }

  private[this] def onMessageEvent(event: MessageEvent): Unit = event.data match {
    case s: String =>
      receivedBytes += s.getBytes.length
      process(decodeJson[T](s) match {
        case Right(x) => x
        case Left(err) => throw err
      })
    case b: Blob =>
      val reader = new FileReader()
      def onLoadEnd(ev: ProgressEvent) = {
        val buff = reader.result
        val ab = buff.asInstanceOf[ArrayBuffer]
        val data = TypedArrayBuffer.wrap(ab)
        receivedBytes += ab.byteLength
        val msg = BinarySerializers.read(data)
        process(msg)
      }
      reader.onloadend = onLoadEnd _
      reader.readAsArrayBuffer(b)
    case buff: ArrayBuffer =>
      val data = TypedArrayBuffer.wrap(buff)
      process(BinarySerializers.read(data))
    case x => throw new IllegalStateException(s"Unhandled message data of type [$x]")
  }

  private[this] def onCloseEvent(event: Event) = {
    connecting = false
    connected = false
    handler.onClose()
    event
  }
}
