package com.kyleu.projectile.models.typescript.input

import com.kyleu.projectile.util.JsonSerializers._

object TypeScriptOptions {
  implicit val jsonEncoder: Encoder[TypeScriptOptions] = deriveEncoder
  implicit val jsonDecoder: Decoder[TypeScriptOptions] = deriveDecoder
}

final case class TypeScriptOptions(files: Seq[String] = Nil)
