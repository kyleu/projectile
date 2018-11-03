package models.output.feature.openapi

import models.export.ExportEnum
import models.output.OutputPath
import models.output.file.JsonFile

object EnumOpenApiSchemaFile {
  def export(e: ExportEnum) = {
    val file = JsonFile(path = OutputPath.OpenAPIJson, dir = "components" +: "schema" +: e.pkg, key = e.propertyName)
    file.add("{", 1)
    file.add(s""""${e.fullClassName}": {""", 1)

    file.add("}", -1)
    file.add("}", -1)
    file
  }
}
