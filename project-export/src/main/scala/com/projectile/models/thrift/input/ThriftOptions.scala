package com.projectile.models.thrift.input

import com.projectile.util.JsonSerializers._

object ThriftOptions {
  implicit val jsonEncoder: Encoder[ThriftOptions] = deriveEncoder
  implicit val jsonDecoder: Decoder[ThriftOptions] = deriveDecoder
}

case class ThriftOptions(files: Seq[String] = Nil)
