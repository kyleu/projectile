package com.projectile.models.export

import com.projectile.models.export.typ.FieldType
import com.projectile.util.JsonSerializers._

object ExportMethod {
  implicit val jsonEncoder: Encoder[ExportMethod] = deriveEncoder
  implicit val jsonDecoder: Decoder[ExportMethod] = deriveDecoder
}

case class ExportMethod(key: String, args: Seq[ExportField], returnType: FieldType)
