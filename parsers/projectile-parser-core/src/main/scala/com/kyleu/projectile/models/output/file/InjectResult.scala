package com.kyleu.projectile.models.output.file

import com.kyleu.projectile.models.output.OutputPath

import com.kyleu.projectile.util.JsonSerializers._

object InjectResult {
  implicit val jsonEncoder: Encoder[InjectResult] = deriveEncoder
  implicit val jsonDecoder: Decoder[InjectResult] = deriveDecoder
}

final case class InjectResult(path: OutputPath, dir: Seq[String], filename: String, status: String, content: String) {
  val filePath = s"${dir.map(_ + "/").mkString}$filename"
  override val toString = s"$path:$filePath"
}
