package com.kyleu.projectile.models.export.typ

import com.kyleu.projectile.util.JsonSerializers._

object FieldTypeRequired {
  implicit val jsonEncoder: Encoder[FieldTypeRequired] = deriveEncoder
  implicit val jsonDecoder: Decoder[FieldTypeRequired] = deriveDecoder
}

final case class FieldTypeRequired(t: FieldType, r: Boolean = false) {
  override def toString = if (r) { t.toString } else { s"Option[$t]" }
}
