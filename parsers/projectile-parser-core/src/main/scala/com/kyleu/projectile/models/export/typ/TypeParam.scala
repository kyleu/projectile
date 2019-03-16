package com.kyleu.projectile.models.export.typ

import com.kyleu.projectile.util.JsonSerializers._

object TypeParam {
  implicit val jsonEncoder: Encoder[TypeParam] = deriveEncoder
  implicit val jsonDecoder: Decoder[TypeParam] = deriveDecoder
}

case class TypeParam(name: String, constraint: Option[FieldType], default: Option[FieldType])
