package com.kyleu.projectile.web.util

import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.util.Logging
import io.circe.parser.decode
import play.api.mvc.WebSocket.MessageFlowTransformer

class MessageFrameFormatter[Req: Decoder, Rsp: Encoder]() extends Logging {
  val stringTransformer = MessageFlowTransformer.stringMessageFlowTransformer.map(s => decode[Req](s) match {
    case Right(x) => x
    case Left(err) => throw err
  }).contramap { rm: Rsp => rm.asJson.spaces2 }

  def transformer() = stringTransformer
}
