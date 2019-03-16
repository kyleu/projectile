package com.kyleu.projectile.models.feature.core.thrift

import com.kyleu.projectile.models.export.ExportEnum
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.EnumFeature
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile

object StringEnumFile {
  def export(config: ExportConfiguration, enum: ExportEnum) = {
    val path = if (enum.features(EnumFeature.Shared)) { OutputPath.SharedSource } else { OutputPath.ServerSource }
    val file = ScalaFile(path = path, dir = enum.pkg, key = enum.className)
    val thriftEnum = enum.pkg.dropRight(1) :+ enum.key

    file.addImport(Seq("enumeratum", "values"), "StringCirceEnum")
    file.addImport(Seq("enumeratum", "values"), "StringEnum")
    file.addImport(Seq("enumeratum", "values"), "StringEnumEntry")

    file.add(s"sealed abstract class ${enum.className}(override val value: String) extends StringEnumEntry {", 1)
    file.add(s"lazy val asThrift = ${thriftEnum.mkString(".")}.apply(toString)")
    file.add("override def toString = value")
    file.add("}", -1)
    file.add()

    file.add(s"object ${enum.className} extends StringEnum[${enum.className}] with StringCirceEnum[${enum.className}] {", 1)
    addFields(enum, file)
    file.add()
    file.add("override val values = findValues")

    file.add(s"def fromThrift(t: ${thriftEnum.mkString(".")}) = ${enum.key}.withValue(t.getValue)")

    file.add("}", -1)

    file
  }

  private[this] def addFields(enum: ExportEnum, file: ScalaFile) = enum.values.foreach { v =>
    val s = v.s.getOrElse(throw new IllegalStateException(s"No string value for enum value [$v]"))
    file.add(s"""case object ${v.className} extends ${enum.className}("$s")""")
  }
}
