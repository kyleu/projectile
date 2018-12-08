package com.projectile.models.feature.core.thrift

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.export.typ.FieldType
import com.projectile.models.export.{ExportField, ExportModel}
import com.projectile.models.feature.ModelFeature
import com.projectile.models.output.OutputPath
import com.projectile.models.output.file.ScalaFile
import com.projectile.models.thrift.input.ThriftFileHelper
import com.projectile.models.thrift.parse.{ThriftFieldScalaHelper, ThriftFieldThriftHelper}

object StructModelFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val path = if (model.features(ModelFeature.Shared)) { OutputPath.SharedSource } else { OutputPath.ServerSource }
    val file = ScalaFile(path = path, dir = model.pkg, key = model.className)

    if (model.features(ModelFeature.Json)) {
      config.addCommonImport(file, "JsonSerializers", "_")
    }

    if (model.features(ModelFeature.DataModel)) {
      config.addCommonImport(file, "DataField")
      config.addCommonImport(file, "DataFieldModel")
    }

    file.add(s"object ${model.className} {", 1)
    if (model.features(ModelFeature.Json)) {
      file.add(s"implicit val jsonEncoder: Encoder[${model.className}] = deriveEncoder")
      file.add(s"implicit val jsonDecoder: Decoder[${model.className}] = deriveDecoder")
      file.add()
    }
    val thriftModel = model.pkg.dropRight(1) :+ model.key
    file.add(s"def fromThrift(t: ${thriftModel.mkString(".")}) = ${model.className}(", 1)
    model.fields.foreach { field =>
      val out = ThriftFieldScalaHelper.getFromField(field)
      val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
      file.add(field.propertyName + " = " + out + comma)
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
      val out = ThriftFieldThriftHelper.getFromField(field).stripSuffix(".toMap")
      val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
      file.add(field.propertyName + " = " + out + comma)
    }
    file.add(")", -1)
    if (model.features(ModelFeature.DataModel)) {
      file.add()
      file.add("override def toDataFields = Seq(", 1)
      model.fields.foreach { field =>
        val x = if (field.required || field.defaultValue.isDefined) {
          val method = if (field.t == FieldType.StringType) { "" } else { ".toString" }
          s"""DataField("${field.propertyName.replaceAllLiterally("`", "")}", Some(${field.propertyName}$method))"""
        } else {
          val method = field.t match {
            case FieldType.StringType => ""
            case FieldType.EnumType(_) => ".map(_.value)"
            case _ => ".map(_.toString)"
          }
          s"""DataField("${field.propertyName}", ${field.propertyName}$method)"""
        }
        val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
        file.add(x + comma)
      }
      file.add(")", -1)
    }
    file.add("}", -1)
    file
  }

  private[this] def addFields(config: ExportConfiguration, pkg: Seq[String], fields: Seq[ExportField], file: ScalaFile) = {
    fields.foreach { field =>
      //field.addImport(file, model.modelPackage)
      val comma = if (fields.lastOption.contains(field)) { "" } else { "," }
      val decl = ThriftFileHelper.declarationFor(config, field.required, field.propertyName, field.defaultValue, field.t)
      file.add(decl + comma)
    }
  }
}
