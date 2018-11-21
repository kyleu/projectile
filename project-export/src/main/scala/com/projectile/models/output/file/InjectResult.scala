package com.projectile.models.output.file

import com.projectile.models.output.OutputPath

import com.projectile.util.JsonSerializers._

object InjectResult {
  implicit val jsonEncoder: Encoder[InjectResult] = deriveEncoder
  implicit val jsonDecoder: Decoder[InjectResult] = deriveDecoder
}

case class InjectResult(path: OutputPath, dir: Seq[String], filename: String, status: String, content: String)
