package models.output.feature.core

import models.export.ExportEnum
import models.output.OutputPath
import models.output.file.ScalaFile

object EnumFile {
  def export(enum: ExportEnum) = {
    val path = OutputPath.ServerSource
    val file = ScalaFile(path = path, dir = "models" +: enum.pkg, key = enum.className)

    file.addImport("enumeratum.values", "StringEnumEntry")
    file.addImport("enumeratum.values", "StringEnum")
    file.addImport("enumeratum.values", "StringCirceEnum")

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

  private[this] def addFields(model: ExportEnum, file: ScalaFile) = model.valuesWithClassNames.foreach { v =>
    file.add(s"""case object ${v._2} extends ${model.className}("${v._1}")""")
  }
}
