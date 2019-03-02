package com.kyleu.projectile.models.export

import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.output.ExportHelper
import com.kyleu.projectile.util.JsonSerializers._

object ExportMethod {
  implicit val jsonEncoder: Encoder[ExportMethod] = deriveEncoder
  implicit val jsonDecoder: Decoder[ExportMethod] = deriveDecoder
}

case class ExportMethod(key: String, args: Seq[ExportField], returnType: FieldType) {
  val name = ExportHelper.toIdentifier(key)
  lazy val signature = s"$name(${args.map(a => s"${a.key}: ${a.t}").mkString(", ")})"
}
