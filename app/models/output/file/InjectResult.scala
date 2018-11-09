package models.output.file

import models.output.OutputPath

import util.JsonSerializers._

object InjectResult {
  implicit val jsonEncoder: Encoder[InjectResult] = deriveEncoder
  implicit val jsonDecoder: Decoder[InjectResult] = deriveDecoder
}

case class InjectResult(path: OutputPath, dir: Seq[String], filename: String, content: String)
