package models.output.feature.slick

import models.export.ExportEnum
import models.export.config.ExportConfiguration
import models.output.OutputPath
import models.output.file.ScalaFile

object ColumnTypeFile {
  def export(config: ExportConfiguration, enum: ExportEnum) = {
    val file = ScalaFile(path = OutputPath.ServerSource, dir = enum.slickPackage, key = enum.className + "ColumnType")

    file.addImport((config.applicationPackage ++ enum.modelPackage).mkString("."), enum.className)
    file.addImport((config.systemPackage ++ Seq("services", "database", "SlickQueryService", "imports")).mkString("."), "_")
    file.addImport("slick.jdbc", "JdbcType")

    file.add(s"object ${enum.className}ColumnType {", 1)
    val ct = s"MappedColumnType.base[${enum.className}, String](_.value, ${enum.className}.withValue)"
    file.add(s"implicit val ${enum.propertyName}ColumnType: JdbcType[${enum.className}] = $ct")
    file.add("}", -1)

    file
  }
}
