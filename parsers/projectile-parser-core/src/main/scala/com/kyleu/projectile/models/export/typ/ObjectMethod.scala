package com.kyleu.projectile.models.export.typ

import com.kyleu.projectile.util.JsonSerializers._

object ObjectMethod {
  implicit val jsonEncoder: Encoder[ObjectMethod] = deriveEncoder
  implicit val jsonDecoder: Decoder[ObjectMethod] = deriveDecoder
}

final case class ObjectMethod(k: String, params: Seq[ObjectField], ret: FieldType)
