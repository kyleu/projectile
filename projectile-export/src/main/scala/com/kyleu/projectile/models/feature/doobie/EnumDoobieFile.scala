package com.kyleu.projectile.models.feature.doobie

import com.kyleu.projectile.models.export.ExportEnum
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.feature.EnumFeature
import com.kyleu.projectile.models.output.file.ScalaFile

object EnumDoobieFile {
  def export(config: ExportConfiguration, enum: ExportEnum) = {
    val path = if (enum.features(EnumFeature.Shared)) { OutputPath.SharedSource } else { OutputPath.ServerSource }
    val file = ScalaFile(path = path, dir = enum.doobiePackage(config), key = enum.className + "Doobie")

    file.addImport(enum.modelPackage(config), enum.className)
    config.addCommonImport(file, "DoobieQueryService", "Imports", "_")

    file.add(s"object ${enum.className}Doobie {", 1)
    val cn = enum.className
    file.add(s"""implicit val ${enum.propertyName}Meta: Meta[$cn] = pgEnumStringOpt("$cn", $cn.withValueOpt, _.value)""")
    file.add("}", -1)

    file
  }
}
