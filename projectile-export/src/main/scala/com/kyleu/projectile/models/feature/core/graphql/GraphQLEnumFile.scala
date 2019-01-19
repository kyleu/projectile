package com.kyleu.projectile.models.feature.core.graphql

import com.kyleu.projectile.models.export.ExportEnum
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.EnumFeature
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}
import com.kyleu.projectile.models.output.file.ScalaFile

object GraphQLEnumFile {
  def export(config: ExportConfiguration, enum: ExportEnum) = {
    val path = if (enum.features(EnumFeature.Shared)) { OutputPath.SharedSource } else { OutputPath.ServerSource }
    val file = ScalaFile(path = path, dir = config.applicationPackage ++ enum.pkg, key = enum.className)

    file.addImport(Seq("enumeratum", "values"), "StringCirceEnum")
    file.addImport(Seq("enumeratum", "values"), "StringEnum")
    file.addImport(Seq("enumeratum", "values"), "StringEnumEntry")

    file.add(s"sealed abstract class ${enum.className}(override val value: String) extends StringEnumEntry {", 1)

    file.add("override def toString = value")
    file.add("}", -1)
    file.add()
    file.add(s"object ${enum.className} extends StringEnum[${enum.className}] with StringCirceEnum[${enum.className}] {", 1)
    enum.values.foreach(v => file.add(s"""case object ${ExportHelper.toClassName(v)} extends ${enum.className}("$v")"""))
    file.add()
    file.add("override val values = findValues")
    file.add("}", -1)

    file
  }
}
