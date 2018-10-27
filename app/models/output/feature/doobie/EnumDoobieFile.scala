package models.output.feature.doobie

import models.export.ExportEnum
import models.export.config.ExportConfiguration
import models.output.OutputPath
import models.output.file.ScalaFile

object EnumDoobieFile {
  def export(config: ExportConfiguration, enum: ExportEnum) = {
    val file = ScalaFile(path = OutputPath.ServerSource, dir = config.applicationPackage ++ enum.doobiePackage, key = enum.className + "Doobie")

    file.addImport((config.applicationPackage ++ enum.modelPackage).mkString("."), enum.className)
    file.addImport((config.systemPackage ++ Seq("services", "database", "doobie", "DoobieQueryService", "Imports")).mkString("."), "_")

    file.add(s"object ${enum.className}Doobie {", 1)
    val cn = enum.className
    file.add(s"""implicit val ${enum.propertyName}Meta: Meta[$cn] = pgEnumStringOpt("$cn", $cn.withValueOpt, _.value)""")
    file.add("}", -1)

    file
  }
}
