package com.kyleu.projectile.models.output

import com.kyleu.projectile.util.JsonSerializers._

case class OutputLog(content: String, occurred: Long)

object OutputLog {
  implicit val jsonEncoder: Encoder[OutputLog] = deriveEncoder
  implicit val jsonDecoder: Decoder[OutputLog] = deriveDecoder
}
