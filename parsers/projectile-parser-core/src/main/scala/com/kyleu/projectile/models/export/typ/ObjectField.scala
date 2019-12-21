package com.kyleu.projectile.models.export.typ

import com.kyleu.projectile.util.JsonSerializers._

object ObjectField {
  implicit val jsonEncoder: Encoder[ObjectField] = deriveEncoder
  implicit val jsonDecoder: Decoder[ObjectField] = deriveDecoder
}

final case class ObjectField(k: String, t: FieldType, req: Boolean = false, readonly: Boolean = false) {
  override def toString = s"$k${if (req) { "" } else { "?" }}: $t"
}
