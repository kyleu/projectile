package com.projectile.models.output.feature.openapi

import com.projectile.models.export.ExportEnum
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.OutputPath
import com.projectile.models.output.file.JsonFile

object EnumOpenApiSchemaFile {
  def export(config: ExportConfiguration, e: ExportEnum) = {
    val file = JsonFile(path = OutputPath.OpenAPIJson, dir = "components" +: "schema" +: e.pkg, key = e.propertyName)
    file.add("{", 1)
    file.add(s""""${(config.applicationPackage ++ e.modelPackage :+ e.className).mkString(".")}": {""", 1)

    file.add("}", -1)
    file.add("}", -1)
    file
  }
}
