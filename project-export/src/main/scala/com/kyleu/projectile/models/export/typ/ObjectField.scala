package com.kyleu.projectile.models.export.typ

import com.kyleu.projectile.util.JsonSerializers._

object ObjectField {
  implicit val jsonEncoder: Encoder[ObjectField] = deriveEncoder
  implicit val jsonDecoder: Decoder[ObjectField] = deriveDecoder
}

case class ObjectField(k: String, v: FieldType)
