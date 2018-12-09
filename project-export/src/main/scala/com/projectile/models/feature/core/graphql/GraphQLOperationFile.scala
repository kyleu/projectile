package com.projectile.models.feature.core.graphql

import com.projectile.models.export.ExportModel
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.ModelFeature
import com.projectile.models.output.OutputPath
import com.projectile.models.output.file.ScalaFile

object GraphQLOperationFile {
  val includeDefaults = false

  def export(config: ExportConfiguration, model: ExportModel) = {
    val path = if (model.features(ModelFeature.Shared)) { OutputPath.SharedSource } else { OutputPath.ServerSource }
    val file = ScalaFile(path = path, dir = config.applicationPackage ++ model.pkg, key = model.className)

    config.addCommonImport(file, "JsonSerializers", "_")

    file.add(s"object ${model.className} {", 1)

    file.add(s"implicit val jsonDecoder: Decoder[${model.className}] = deriveDecoder")

    GraphQLObjectHelper.addArguments(config, file, model.arguments)

    file.add("}", -1)
    file.add()

    file.add(s"case class ${model.className}(", 2)
    GraphQLObjectHelper.addFields(config, file, model.fields)
    file.add(")", -2)

    file
  }
}
