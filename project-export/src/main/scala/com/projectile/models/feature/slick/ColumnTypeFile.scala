package com.projectile.models.feature.slick

import com.projectile.models.export.ExportEnum
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.OutputPath
import com.projectile.models.feature.EnumFeature
import com.projectile.models.output.file.ScalaFile

object ColumnTypeFile {
  def export(config: ExportConfiguration, enum: ExportEnum) = {
    val path = if (enum.features(EnumFeature.Shared)) { OutputPath.SharedSource } else { OutputPath.ServerSource }
    val file = ScalaFile(path = path, dir = config.applicationPackage ++ enum.slickPackage, key = enum.className + "ColumnType")

    file.addImport(config.applicationPackage ++ enum.modelPackage, enum.className)
    config.addCommonImport(file, "SlickQueryService", "imports", "_")
    file.addImport(Seq("slick", "jdbc"), "JdbcType")

    file.add(s"object ${enum.className}ColumnType {", 1)
    val ct = s"MappedColumnType.base[${enum.className}, String](_.value, ${enum.className}.withValue)"
    file.add(s"implicit val ${enum.propertyName}ColumnType: JdbcType[${enum.className}] = $ct")
    file.add("}", -1)

    file
  }
}
