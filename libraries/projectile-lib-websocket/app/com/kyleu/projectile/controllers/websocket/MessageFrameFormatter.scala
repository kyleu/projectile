package com.kyleu.projectile.controllers.websocket

import java.nio.ByteBuffer

import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.util.BinarySerializers._
import com.kyleu.projectile.util.Logging
import io.circe.parser.decode
import play.api.mvc.WebSocket.MessageFlowTransformer

class MessageFrameFormatter[Req: Decoder: Pickler, Rsp: Encoder: Pickler]() extends Logging {
  val stringTransformer = MessageFlowTransformer.stringMessageFlowTransformer.map(s => decode[Req](s) match {
    case Right(x) => x
    case Left(err) => throw err
  }).contramap { rm: Rsp => rm.asJson.spaces2 }

  val binaryTransformer = MessageFlowTransformer.byteArrayMessageFlowTransformer.map { ba =>
    Unpickle[Req].fromBytes(ByteBuffer.wrap(ba))
  }.contramap { rm: Rsp => toByteArray(Pickle.intoBytes(rm)) }

  def transformer(binary: Boolean) = if (binary) { binaryTransformer } else { stringTransformer }
}
