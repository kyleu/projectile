package com.projectile.models.feature.core.graphql

import com.projectile.models.export.{ExportField, ExportModel}
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.export.typ.FieldTypeAsScala
import com.projectile.models.output.file.ScalaFile

object GraphQLObjectHelper {
  def objectFor(config: ExportConfiguration, file: ScalaFile, m: ExportModel, incEncoder: Boolean = false): Unit = {
    file.add(s"object ${m.className} {", 1)

    file.add(s"implicit val jsonDecoder: Decoder[${m.className}] = deriveDecoder")
    if (incEncoder) {
      file.add(s"implicit val jsonEncoder: Encoder[${m.className}] = deriveEncoder")
    }
    file.add("}", -1)
    file.add()
    file.add(s"case class ${m.className}(", 2)
    addFields(config, file, m.fields)
    file.add(")", -2)
  }

  private[this] def addFields(config: ExportConfiguration, file: ScalaFile, fields: Seq[ExportField]) = {
    fields.foreach { f =>
      f.addImport(config, file, Nil)
      val param = s"${f.propertyName}: ${FieldTypeAsScala.asScala(config, f.t)}"
      val comma = if (fields.lastOption.contains(f)) { "" } else { "," }
      file.add(param + comma)
    }
  }
}
