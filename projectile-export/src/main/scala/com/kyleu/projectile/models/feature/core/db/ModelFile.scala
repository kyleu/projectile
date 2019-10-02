// scalastyle:off file.size.limit
package com.kyleu.projectile.models.feature.core.db

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.export.{ExportField, ExportModel}
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.feature.core.ModelHelper
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile

object ModelFile {
  val includeDefaults = false

  def export(config: ExportConfiguration, model: ExportModel) = {
    val path = if (model.features(ModelFeature.Shared)) { OutputPath.SharedSource } else { OutputPath.ServerSource }
    val file = ScalaFile(path = path, dir = model.modelPackage(config), key = model.className)

    if (model.features(ModelFeature.DataModel)) {
      config.addCommonImport(file, "DataField")
      config.addCommonImport(file, "DataSummary")
      config.addCommonImport(file, "DataFieldModel")
    }
    if (model.features(ModelFeature.Json)) {
      config.addCommonImport(file, "JsonSerializers", "_")
    }
    if (model.features(ModelFeature.ScalaJS)) {
      file.addImport(Seq("scala", "scalajs", "js", "annotation"), "JSExport")
      file.addImport(Seq("scala", "scalajs", "js", "annotation"), "JSExportTopLevel")
    }

    file.add(s"object ${model.className} {", 1)
    ModelHelper.addJson(config, file, model)
    ModelHelper.addEmpty(config, model, file)
    file.add("}", -1)
    file.add()

    model.description.foreach(d => file.add(s"/** $d */"))

    if (model.features(ModelFeature.ScalaJS)) {
      file.add(s"""@JSExportTopLevel(util.Config.projectId + ".${model.className}")""")
    }
    file.add(s"final case class ${model.className}(", 2)
    ModelHelper.addFields(config, model, file)

    if (model.features(ModelFeature.DataModel)) {
      model.extendsClass match {
        case Some(x) => file.add(") extends " + x + " {", -2)
        case None => file.add(") extends DataFieldModel {", -2)
      }
      file.indent()
      file.add("override def toDataFields = Seq(", 1)
      model.fields.foreach { field =>
        process(file, field, model.fields.lastOption.contains(field))
      }
      file.add(")", -1)
      file.add()
      val pk = if (model.pkFields.isEmpty) { "\"no-pk\"" } else { model.pkFields.map(f => f.propertyName + ".toString").mkString(" + \"/\" + ") }

      file.add(s"""def toSummary = DataSummary(model = "${model.propertyName}", pk = $pk, entries = Map(""", 1)
      val fields = (model.pkFields ++ model.summaryFields).distinct
      val mapped = fields.map {
        case f if f.required && f.t == FieldType.StringType => s""""${f.title}" -> Some(${f.propertyName})"""
        case f if f.required => s""""${f.title}" -> Some(${f.propertyName}.toString)"""
        case f if f.t == FieldType.StringType => s""""${f.title}" -> ${f.propertyName}"""
        case f => s""""${f.title}" -> ${f.propertyName}.map(_.toString)"""
      }
      mapped.foreach(x => file.add(x + (if (mapped.lastOption.contains(x)) { "" } else { "," })))
      file.add("))", -1)

      file.add("}", -1)
    } else {
      model.extendsClass match {
        case Some(x) => file.add(") extends " + x, -2)
        case None => file.add(")", -2)
      }
    }
    file
  }

  private[this] def process(file: ScalaFile, field: ExportField, last: Boolean) = {
    val x = if (field.required) {
      val method = field.t match {
        case FieldType.StringType | FieldType.EncryptedStringType => field.propertyName
        case FieldType.EnumType(_) => s"${field.propertyName}.value"
        case FieldType.ListType(_) => s""""{ " + ${field.propertyName}.mkString(", ") + " }""""
        case _ => s"${field.propertyName}.toString"
      }
      s"""DataField("${field.propertyName}", Some($method))"""
    } else {
      val method = field.t match {
        case FieldType.StringType | FieldType.EncryptedStringType => field.propertyName
        case FieldType.EnumType(_) => s"${field.propertyName}.map(_.value)"
        case FieldType.ListType(_) => s"""${field.propertyName}.map(v => "{ " + v.mkString(", ") + " }")"""
        case _ => s"${field.propertyName}.map(_.toString)"
      }
      s"""DataField("${field.propertyName}", $method)"""
    }
    val comma = if (last) { "" } else { "," }
    file.add(x + comma)
  }
}
