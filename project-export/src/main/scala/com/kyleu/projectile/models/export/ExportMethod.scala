package com.kyleu.projectile.models.export

import com.kyleu.projectile.models.export.typ.{FieldType, FieldTypeAsScala}
import com.kyleu.projectile.util.JsonSerializers._

object ExportMethod {
  implicit val jsonEncoder: Encoder[ExportMethod] = deriveEncoder
  implicit val jsonDecoder: Decoder[ExportMethod] = deriveDecoder
}

case class ExportMethod(key: String, args: Seq[ExportField], returnType: FieldType) {
  lazy val signature = s"$key(${args.map(a => s"${a.key}: ${a.t}").mkString(", ")})"
}
