package com.kyleu.projectile.models.feature.core.graphql

import com.kyleu.projectile.models.export.{ExportField, ExportModel}
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldTypeAsScala
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile

object GraphQLInputFile {
  val includeDefaults = false

  def export(config: ExportConfiguration, model: ExportModel) = {
    val path = if (model.features(ModelFeature.Shared)) { OutputPath.SharedSource } else { OutputPath.ServerSource }
    val file = ScalaFile(path = path, dir = config.applicationPackage ++ model.pkg, key = model.className)

    config.addCommonImport(file, "JsonSerializers", "_")

    file.add(s"object ${model.className} {", 1)
    file.add(s"implicit val jsonDecoder: Decoder[${model.className}] = deriveDecoder")
    file.add(s"implicit val jsonEncoder: Encoder[${model.className}] = deriveEncoder")
    file.add("}", -1)

    file.add()
    file.add(s"case class ${model.className}(", 2)
    addInputFields(config, file, model.fields)
    file.add(")", -2)

    file
  }

  private[this] def addInputFields(config: ExportConfiguration, file: ScalaFile, fields: Seq[ExportField]) = {
    fields.foreach { f =>
      f.addImport(config, file, Nil)
      val t = if (f.required) {
        FieldTypeAsScala.asScala(config, f.t)
      } else {
        "Option[" + FieldTypeAsScala.asScala(config, f.t) + "] = None"
      }
      val param = s"${f.propertyName}: $t"
      val comma = if (fields.lastOption.contains(f)) { "" } else { "," }
      file.add(param + comma)
    }
  }

}
