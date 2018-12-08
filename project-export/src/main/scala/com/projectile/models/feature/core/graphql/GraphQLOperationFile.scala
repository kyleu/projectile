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

    GraphQLObjectHelper.objectFor(config, file, model)

    file
  }
}
