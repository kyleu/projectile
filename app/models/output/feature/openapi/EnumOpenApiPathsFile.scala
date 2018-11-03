package models.output.feature.openapi

import models.export.ExportEnum
import models.output.OutputPath
import models.output.file.JsonFile

object EnumOpenApiPathsFile {
  def export(enum: ExportEnum) = {
    val file = JsonFile(path = OutputPath.OpenAPIJson, dir = "paths" +: enum.pkg, key = enum.propertyName)
    file.add("{", 1)

    val route = s"/admin/${enum.pkg.mkString("/")}/${enum.propertyName}"
    file.add(s""""$route": {""", 1)
    file.add("\"get\": {", 1)
    file.add("\"summary\": \"Lists the possible " + enum.className + " values.\",")
    file.add("\"operationId\": \"" + enum.fullClassName + ".list\",")
    file.add("\"tags\": [\"" + enum.pkg.mkString(".") + "\"],")
    file.add("\"responses\": {", 1)
    file.add("\"200\": {", 1)
    file.add("\"content\": {", 1)
    file.add("\"application/json\": {", 1)
    file.add("\"schema\": {", 1)
    file.add("\"type\": \"array\",")
    file.add("\"items\": {", 1)
    file.add("\"type\": \"string\"")
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)

    file.add("}", -1)
    file.add("}", -1)

    file.add("}", -1)
    file
  }
}
