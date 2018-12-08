package com.projectile.models.feature.graphql.thrift

import com.projectile.models.export.ExportEnum
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.input.EnumInputType
import com.projectile.models.output.OutputPath
import com.projectile.models.output.file.ScalaFile

object ThriftEnumSchemaFile {
  def export(config: ExportConfiguration, enum: ExportEnum) = {
    val file = ScalaFile(path = OutputPath.ServerSource, enum.pkg :+ "graphql", enum.className + "Schema")

    val t = enum.inputType match {
      case EnumInputType.ThriftIntEnum => "Int"
      case EnumInputType.ThriftStringEnum => "String"
      case _ => throw new IllegalStateException(s"Cannot process [${enum.inputType}]")
    }

    config.addCommonImport(file, "CommonSchema", s"derive${t}EnumeratumType")

    file.addImport(enum.pkg, enum.className)
    file.addImport(Seq("sangria", "schema"), "EnumType")

    file.add(s"""object ${enum.className}Schema {""", 1)
    val derive = s"""derive${t}EnumeratumType("${enum.className}", ${enum.className}.values)"""
    file.add(s"""implicit val ${enum.propertyName}Type: EnumType[${enum.className}] = $derive""")
    file.add("}", -1)

    file
  }
}
