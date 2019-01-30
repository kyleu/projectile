package com.kyleu.projectile.models.feature.slick

import com.kyleu.projectile.models.export.ExportEnum
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile

object ColumnTypeFile {
  def export(config: ExportConfiguration, enum: ExportEnum) = {
    val file = ScalaFile(path = OutputPath.ServerSource, dir = enum.slickPackage(config), key = enum.className + "ColumnType")

    file.addImport(enum.modelPackage(config), enum.className)
    config.addCommonImport(file, "SlickQueryService", "imports", "_")
    file.addImport(Seq("slick", "jdbc"), "JdbcType")

    file.add(s"object ${enum.className}ColumnType {", 1)
    val ct = s"MappedColumnType.base[${enum.className}, String](_.value, ${enum.className}.withValue)"
    file.add(s"implicit val ${enum.propertyName}ColumnType: JdbcType[${enum.className}] = $ct")
    file.add("}", -1)

    file
  }
}
