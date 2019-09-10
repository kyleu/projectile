package com.kyleu.projectile.models.feature.core.thrift

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.export.{ExportField, ExportModel}
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.feature.core.ModelHelper
import com.kyleu.projectile.models.input.InputType
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.thrift.input.ThriftFileHelper
import com.kyleu.projectile.models.thrift.parse.{ThriftFieldScalaHelper, ThriftFieldThriftHelper}

object StructModelFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val path = if (model.features(ModelFeature.Shared)) { OutputPath.SharedSource } else { OutputPath.ServerSource }
    val file = ScalaFile(path = path, dir = model.pkg, key = model.className)
    if (model.features(ModelFeature.Json)) { config.addCommonImport(file, "JsonSerializers", "_") }
    if (model.features(ModelFeature.DataModel)) {
      config.addCommonImport(file, "DataField")
      config.addCommonImport(file, "DataFieldModel")
    }
    if (model.fields.isEmpty) { exportEmpty(config, model, file) } else { exportFields(config, model, file) }
    file
  }

  private[this] def exportEmpty(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val thriftModel = model.pkg.dropRight(1) :+ model.key
    file.add(s"object ${model.className} {", 1)
    ModelHelper.addJsonEmpty(config, file, model)
    file.add(s"def fromThrift(t: ${thriftModel.mkString(".")}) = ${model.className}()")
    file.add("}", -1)
    file.add()
    file.add(s"final case class ${model.className}() {", 1)
    file.add(s"lazy val asThrift = ${thriftModel.mkString(".")}()")
    file.add("}", -1)
  }

  private[this] def exportFields(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    file.add(s"object ${model.className} {", 1)
    ModelHelper.addJson(config, file, model, isThrift = true)
    val thriftModel = model.pkg.dropRight(1) :+ model.key
    file.add(s"def fromThrift(t: ${thriftModel.mkString(".")}) = ${model.className}(", 1)
    model.fields.foreach { field =>
      field.addImport(config, file, model.pkg, isThrift = true)
      val out = ThriftFieldScalaHelper.getFromField(field)
      val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
      file.add(ExportHelper.escapeKeyword(field.propertyName) + " = " + out + comma)
    }
    file.add(")", -1)
    file.add("}", -1)
    file.add()
    file.add(s"final case class ${model.className}(", 2)
    addFields(config, model.pkg, model.fields, file)
    if (model.features(ModelFeature.DataModel)) {
      file.add(") extends DataFieldModel {", -2)
    } else {
      file.add(") {", -2)
    }
    file.indent()
    file.add(s"lazy val asThrift = ${thriftModel.mkString(".")}(", 1)
    model.fields.foreach { field =>
      val out = ThriftFieldThriftHelper.getFromField(field)
      val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
      file.add(ExportHelper.escapeKeyword(field.propertyName) + " = " + out + comma)
    }
    file.add(")", -1)
    if (model.features(ModelFeature.DataModel)) {
      file.add()
      file.add("override def toDataFields = Seq(", 1)
      model.fields.foreach { field =>
        val x = if (field.required) {
          val method = if (field.t == FieldType.StringType) { "" } else { ".toString" }
          s"""DataField("${field.propertyName.replaceAllLiterally("`", "")}", Some(${ExportHelper.escapeKeyword(field.propertyName)}$method))"""
        } else {
          val method = field.t match {
            case FieldType.StringType => ""
            case FieldType.EnumType(key) => config.getEnum(key, "model file").inputType match {
              case InputType.Enum.ThriftIntEnum => ".map(_.value.toString)"
              case _ => ".map(_.value)"
            }
            case _ => ".map(_.toString)"
          }
          s"""DataField("${field.propertyName}", ${ExportHelper.escapeKeyword(field.propertyName)}$method)"""
        }
        val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
        file.add(x + comma)
      }
      file.add(")", -1)
    }
    file.add("}", -1)
  }
  private[this] def addFields(config: ExportConfiguration, pkg: Seq[String], fields: Seq[ExportField], file: ScalaFile) = fields.foreach { field =>
    field.addImport(config, file, pkg, isThrift = true)
    val comma = if (fields.lastOption.contains(field)) { "" } else { "," }
    val decl = ThriftFileHelper.declarationFor(config, field.required, field.propertyName, field.defaultValue, field.t)
    file.add(decl + comma)
  }
}
