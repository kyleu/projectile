package com.projectile.models.feature.core.graphql

import com.projectile.models.export.{ExportField, ExportModel}
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.export.typ.FieldTypeAsScala
import com.projectile.models.feature.ModelFeature
import com.projectile.models.output.OutputPath
import com.projectile.models.output.file.ScalaFile

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
      val param = s"${f.propertyName}: ${FieldTypeAsScala.asScala(config, f.t)}"
      val comma = if (fields.lastOption.contains(f)) { "" } else { "," }
      file.add(param + comma)
    }
  }

}
