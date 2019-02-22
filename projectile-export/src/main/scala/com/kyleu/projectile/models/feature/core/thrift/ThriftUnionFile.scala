package com.kyleu.projectile.models.feature.core.thrift

import com.kyleu.projectile.models.export.ExportUnion
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldTypeAsScala
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}

object ThriftUnionFile {
  def export(config: ExportConfiguration, union: ExportUnion) = {
    val file = ScalaFile(path = OutputPath.ServerSource, dir = union.pkg, key = union.className)
    val thriftUnion = union.pkg.dropRight(1) :+ union.className
    val tc = thriftUnion.mkString(".")

    file.add(s"sealed trait ${union.className}")
    file.add()

    file.add(s"object ${union.className} {", 1)
    addFields(config, union, file)
    file.add()
    file.add(s"def fromThrift(t: $tc) = t match {", 1)
    union.types.foreach { t =>
      val s = FieldTypeAsScala.asScala(config, t.t)
      file.add(s"case $tc.${t.className}(x) => $s.fromThrift(x)")
    }
    file.add(s"""case $tc.UnknownUnionField(_) => throw new IllegalStateException("Unknown union field")""")
    file.add("}", -1)
    file.add("}", -1)

    file
  }

  private[this] def addFields(config: ExportConfiguration, union: ExportUnion, file: ScalaFile) = union.types.foreach { t =>
    t.addImport(config, file, union.pkg)
    val s = FieldTypeAsScala.asScala(config, t.t)
    file.add(s"""case class ${ExportHelper.toClassName(t.className)}(v: $s) extends ${union.className}""")
  }
}
