package com.kyleu.projectile.models.output

import com.kyleu.projectile.util.JsonSerializers._

object OutputLog {
  implicit val jsonEncoder: Encoder[OutputLog] = deriveEncoder
  implicit val jsonDecoder: Decoder[OutputLog] = deriveDecoder
}

final case class OutputLog(content: String, occurred: Long)
