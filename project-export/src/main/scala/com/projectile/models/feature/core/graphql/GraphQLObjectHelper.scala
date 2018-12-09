package com.projectile.models.feature.core.graphql

import com.projectile.models.export.ExportField
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.export.typ.{FieldTypeAsScala, FieldTypeImports, ObjectField}
import com.projectile.models.output.file.ScalaFile
import sangria.ast.OperationDefinition

import scala.io.Source

object GraphQLObjectHelper {
  def addFields(config: ExportConfiguration, file: ScalaFile, fields: Seq[ExportField]) = {
    fields.foreach { f =>
      f.addImport(config, file, Nil)
      val param = s"${f.propertyName}: ${FieldTypeAsScala.asScala(config, f.t)}"
      val comma = if (fields.lastOption.contains(f)) { "" } else { "," }
      file.add(param + comma)
    }
  }

  def addObjectFields(config: ExportConfiguration, file: ScalaFile, fields: Seq[ObjectField]) = {
    fields.foreach { f =>
      FieldTypeImports.imports(config, f.v).foreach(pkg => file.addImport(pkg.init, pkg.last))
      val param = s"${f.k}: ${FieldTypeAsScala.asScala(config, f.v)}"
      val comma = if (fields.lastOption.contains(f)) { "" } else { "," }
      file.add(param + comma)
    }
  }

  def addArguments(config: ExportConfiguration, file: ScalaFile, arguments: Seq[ExportField]) = if (arguments.nonEmpty) {
    file.addImport(Seq("io", "circe"), "Json")
    val args = arguments.map { f =>
      f.addImport(config, file, Nil)
      val typ = FieldTypeAsScala.asScala(config, f.t)
      val dv = f.defaultValue.map(" = " + _).getOrElse("")
      s"${f.propertyName}: $typ$dv"
    }.mkString(", ")
    val varsDecl = arguments.map(v => s""""${v.propertyName}" -> ${v.propertyName}.asJson""").mkString(", ")
    file.add(s"def variables($args) = {", 1)
    file.add(s"Json.obj($varsDecl)")
    file.add("}", -1)
    file.add()
  }

  def addContent(file: ScalaFile, op: OperationDefinition) = {
    file.add("override val content = \"\"\"", 1)
    Source.fromString(op.renderPretty).getLines.foreach(l => file.add("|" + l))
    file.add("\"\"\".stripMargin.trim", -1)
  }
}
