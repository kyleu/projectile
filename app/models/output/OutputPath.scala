package models.output

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class OutputPath(override val value: String, val description: String) extends StringEnumEntry

object OutputPath extends StringEnum[OutputPath] with StringCirceEnum[OutputPath] {
  case object Root extends OutputPath("root", "The main directory where all files are generated")
  case object ServerSource extends OutputPath("server", "Scala source root for server-only classes")
  case object ServerResource extends OutputPath("serverResources", "Scala resource root for server-only files")
  case object ServerTest extends OutputPath("serverTest", "Scala source root for server-only test classes")
  case object SharedSource extends OutputPath("shared", "Scala source root for shared classes")
  case object SharedTest extends OutputPath("sharedTest", "Scala source root for shared test classes")
  case object OpenAPIJson extends OutputPath("openApi", "Directory that will contain OpenAPI/Swagger definitions")
  case object ThriftOutput extends OutputPath("thrift", "Location to store generated thrift models and services")
  case object WikiMarkdown extends OutputPath("wiki", "Location to store generated wiki documentation")

  override val values = findValues
}
