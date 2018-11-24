package com.projectile.models.thrift.schema

import com.projectile.util.JsonSerializers._

object ThriftEnum {
  implicit val jsonEncoder: Encoder[ThriftEnum] = deriveEncoder
  implicit val jsonDecoder: Decoder[ThriftEnum] = deriveDecoder
}

case class ThriftEnum(
    key: String,
    values: Seq[String]
)
