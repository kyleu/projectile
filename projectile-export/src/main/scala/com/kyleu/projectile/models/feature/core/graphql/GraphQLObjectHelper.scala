package com.kyleu.projectile.models.feature.core.graphql

import com.kyleu.projectile.models.export.ExportField
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.{FieldTypeAsScala, FieldTypeImports, ObjectField}
import com.kyleu.projectile.models.output.file.ScalaFile

object GraphQLObjectHelper {
  def addFields(config: ExportConfiguration, file: ScalaFile, fields: Seq[ExportField]) = {
    fields.foreach { f =>
      f.addImport(config, file, Nil)
      val t = if (f.required) {
        FieldTypeAsScala.asScala(config, f.t)
      } else {
        s"Option[${FieldTypeAsScala.asScala(config, f.t)}]"
      }
      val param = s"${f.propertyName}: $t"
      val comma = if (fields.lastOption.contains(f)) { "" } else { "," }
      file.add(param + comma)
    }
  }

  def addObjectFields(config: ExportConfiguration, file: ScalaFile, fields: Seq[ObjectField]) = {
    fields.foreach { f =>
      FieldTypeImports.imports(config, f.t).foreach(pkg => file.addImport(pkg.init, pkg.lastOption.getOrElse(throw new IllegalStateException())))
      val t = if (f.req) {
        FieldTypeAsScala.asScala(config, f.t)
      } else {
        s"Option[${FieldTypeAsScala.asScala(config, f.t)}]"
      }
      val param = s"${f.k}: $t"
      val comma = if (fields.lastOption.contains(f)) { "" } else { "," }
      file.add(param + comma)
    }
  }

  def addArguments(config: ExportConfiguration, file: ScalaFile, arguments: Seq[ExportField]) = if (arguments.nonEmpty) {
    file.addImport(Seq("io", "circe"), "Json")
    val args = arguments.map { f =>
      f.addImport(config, file, Nil)
      val typ = FieldTypeAsScala.asScala(config, f.t)
      val (t, dv) = if (f.required) {
        typ -> f.defaultValue.map(" = " + _).getOrElse("")
      } else {
        s"Option[$typ]" -> f.defaultValue.map(" = Some(" + _ + ")").getOrElse(" = None")
      }
      s"${f.propertyName}: $t$dv"
    }.mkString(", ")
    val varsDecl = arguments.map(v => s""""${v.propertyName}" -> ${v.propertyName}.asJson""").mkString(", ")
    file.add(s"def variables($args) = {", 1)
    file.add(s"Json.obj($varsDecl)")
    file.add("}", -1)
    file.add()
  }
}
