package com.kyleu.projectile.models.feature.core.thrift

import com.kyleu.projectile.models.export.ExportUnion
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.{FieldTypeAsScala, FieldTypeImports}
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}

object ThriftUnionFile {
  def export(config: ExportConfiguration, union: ExportUnion) = {
    val file = ScalaFile(path = OutputPath.ServerSource, dir = union.pkg, key = union.className)
    val tc = (union.pkg.dropRight(1) :+ union.className).mkString(".")

    file.add(s"sealed trait ${union.className} {", 1)
    file.add(s"def asThrift: $tc")
    file.add("}", -1)
    file.add()

    file.add(s"object ${union.className} {", 1)
    addFields(config, union, file)
    file.add()

    config.addCommonImport(file, "JsonSerializers", "_")
    file.add(s"implicit val jsonEncoder: Encoder[${union.className}] = deriveEncoder")
    file.add(s"implicit val jsonDecoder: Decoder[${union.className}] = deriveDecoder")
    file.add()

    file.add(s"def fromThrift(t: $tc) = t match {", 1)
    union.types.foreach { t =>
      val cn = FieldTypeImports.imports(config, t.t, isThrift = true).headOption.map(_.mkString(".")).getOrElse {
        FieldTypeAsScala.asScala(config, t.t, isThrift = true)
      }
      file.add(s"case $tc.${t.className}(x) => ${t.className}($cn.fromThrift(x))")
    }
    file.add(s"""case $tc.UnknownUnionField(_) => throw new IllegalStateException("Unknown union field")""")
    file.add("}", -1)
    file.add("}", -1)

    file
  }

  private[this] def addFields(config: ExportConfiguration, union: ExportUnion, file: ScalaFile) = union.types.foreach { t =>
    val tc = (union.pkg.dropRight(1) :+ union.className).mkString(".")

    val cn = FieldTypeImports.imports(config, t.t, isThrift = true).headOption.map(_.mkString(".")).getOrElse {
      FieldTypeAsScala.asScala(config, t.t, isThrift = true)
    }
    file.add(s"""final case class ${ExportHelper.toClassName(t.className)}(${t.propertyName}: $cn) extends ${union.className} {""", 1)
    file.add(s"override def asThrift = $tc.${t.className}(${t.propertyName}.asThrift)")
    file.add("}", -1)
  }
}
