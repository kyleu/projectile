package com.kyleu.projectile.models.thrift.input

import com.kyleu.projectile.util.JsonSerializers._

object ThriftOptions {
  implicit val jsonEncoder: Encoder[ThriftOptions] = deriveEncoder
  implicit val jsonDecoder: Decoder[ThriftOptions] = deriveDecoder
}

final case class ThriftOptions(files: Seq[String] = Nil)
