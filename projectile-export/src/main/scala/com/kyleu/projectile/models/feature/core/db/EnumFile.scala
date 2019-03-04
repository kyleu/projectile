package com.kyleu.projectile.models.feature.core.db

import com.kyleu.projectile.models.export.ExportEnum
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.EnumFeature
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile

object EnumFile {
  def export(config: ExportConfiguration, enum: ExportEnum) = {
    val path = if (enum.features(EnumFeature.Shared)) { OutputPath.SharedSource } else { OutputPath.ServerSource }
    val file = ScalaFile(path = path, dir = enum.modelPackage(config), key = enum.className)

    file.addImport(Seq("enumeratum", "values"), "StringEnumEntry")
    file.addImport(Seq("enumeratum", "values"), "StringEnum")
    file.addImport(Seq("enumeratum", "values"), "StringCirceEnum")

    file.add(s"sealed abstract class ${enum.className}(override val value: String) extends StringEnumEntry {", 1)
    file.add("override def toString = value")
    file.add("}", -1)
    file.add()

    file.add(s"object ${enum.className} extends StringEnum[${enum.className}] with StringCirceEnum[${enum.className}] {", 1)
    addFields(enum, file)
    file.add()
    file.add("override val values = findValues")
    file.add("}", -1)

    file
  }

  private[this] def addFields(model: ExportEnum, file: ScalaFile) = model.values.foreach { v =>
    file.add(s"""case object ${v.className} extends ${model.className}("${v.v}")""")
  }
}
