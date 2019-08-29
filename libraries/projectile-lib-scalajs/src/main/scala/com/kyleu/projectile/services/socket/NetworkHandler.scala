package com.kyleu.projectile.services.socket

import com.kyleu.projectile.util.BinarySerializers
import com.kyleu.projectile.util.BinarySerializers.Pickler
import com.kyleu.projectile.util.JsonSerializers.{Decoder, decodeJson}
import org.scalajs.dom.raw.{Blob, FileReader, MessageEvent, ProgressEvent}

import scala.scalajs.js.typedarray.{ArrayBuffer, TypedArrayBuffer}

class NetworkHandler[T: Decoder: Pickler](process: T => Unit) {
  private[this] var receivedBytes = 0

  def onMessageEvent(event: MessageEvent): Unit = event.data match {
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
}
