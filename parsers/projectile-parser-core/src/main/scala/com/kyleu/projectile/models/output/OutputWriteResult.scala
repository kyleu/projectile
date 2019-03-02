package com.kyleu.projectile.models.output

import com.kyleu.projectile.util.JsonSerializers._

object OutputWriteResult {
  implicit val jsonEncoder: Encoder[OutputWriteResult] = deriveEncoder
  implicit val jsonDecoder: Decoder[OutputWriteResult] = deriveDecoder
}

case class OutputWriteResult(file: String, path: String, logs: Seq[String])
