package com.kyleu.projectile.models.feature.graphql

import com.kyleu.projectile.models.export.ExportEnum
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.input.InputType
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile

object EnumSchemaFile {
  def export(config: ExportConfiguration, enum: ExportEnum) = {
    val t = enum.inputType match {
      case InputType.Enum.ThriftIntEnum => "Int"
      case _ => "String"
    }

    val file = ScalaFile(path = OutputPath.ServerSource, enum.modelPackage(config), enum.className + "Schema")

    config.addCommonImport(file, "CommonSchema")
    config.addCommonImport(file, "GraphQLContext")
    config.addCommonImport(file, "GraphQLSchemaHelper")

    file.addImport(Seq("sangria", "schema"), "EnumType")
    file.addImport(Seq("sangria", "schema"), "ListType")
    file.addImport(Seq("sangria", "schema"), "fields")
    file.addImport(Seq("scala", "concurrent"), "Future")

    file.add(s"""object ${enum.className}Schema extends GraphQLSchemaHelper("${enum.propertyName}") {""", 1)
    file.add(s"implicit val ${enum.propertyName}EnumType: EnumType[${enum.className}] = CommonSchema.derive${t}EnumeratumType(", 1)
    file.add(s"""name = "${enum.className}",""")
    file.add(s"values = ${enum.className}.values")
    file.add(")", -1)
    file.add()

    file.add("val queryFields = fields(", 1)
    val r = s"""Future.successful(${enum.className}.values)"""
    file.add(s"""unitField(name = "${enum.propertyName}", desc = None, t = ListType(${enum.propertyName}EnumType), f = (_, _) => $r)""")
    file.add(")", -1)
    file.add("}", -1)

    file
  }
}
