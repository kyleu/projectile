package com.projectile.models.feature.core.db

import com.projectile.models.export.{ExportField, ExportModel, FieldType}
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.ModelFeature
import com.projectile.models.output.OutputPath
import com.projectile.models.output.file.ScalaFile

object ModelFile {
  val includeDefaults = false

  def export(config: ExportConfiguration, model: ExportModel) = {
    val path = if (model.features(ModelFeature.Shared)) { OutputPath.SharedSource } else { OutputPath.ServerSource }
    val file = ScalaFile(path = path, dir = config.applicationPackage ++ model.modelPackage, key = model.className)

    if (model.features(ModelFeature.DataModel)) {
      val pkg = config.resultsPackage :+ "data"
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
    if (model.features(ModelFeature.Json)) {
      file.add(s"implicit val jsonEncoder: Encoder[${model.className}] = deriveEncoder")
      file.add(s"implicit val jsonDecoder: Decoder[${model.className}] = deriveDecoder")
      file.add()
    }
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
      val title = if (model.summaryFields.isEmpty) {
        model.pkFields.map(f => "$" + f.propertyName).mkString(", ")
      } else {
        model.summaryFields.map(f => "$" + f.propertyName + "").mkString(" / ") + " (" + model.pkFields.map(f => "$" + f.propertyName + "").mkString(", ") + ")"
      }
      val pk = model.pkFields.map(f => f.propertyName + ".toString").mkString(", ")
      file.add(s"""def toSummary = DataSummary(model = "${model.propertyName}", pk = Seq($pk), title = s"$title")""")

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
    val x = if (field.notNull) {
      val method = field.t match {
        case FieldType.StringType | FieldType.EncryptedStringType => field.propertyName
        case FieldType.EnumType => s"${field.propertyName}.value"
        case FieldType.ArrayType => s""""{ " + ${field.propertyName}.mkString(", ") + " }""""
        case _ => s"${field.propertyName}.toString"
      }
      s"""DataField("${field.propertyName}", Some($method))"""
    } else {
      val method = field.t match {
        case FieldType.StringType | FieldType.EncryptedStringType => field.propertyName
        case FieldType.EnumType => s"${field.propertyName}.map(_.value)"
        case FieldType.ArrayType => s"""${field.propertyName}.map(v => "{ " + v.mkString(", ") + " }")"""
        case _ => s"${field.propertyName}.map(_.toString)"
      }
      s"""DataField("${field.propertyName}", $method)"""
    }
    val comma = if (last) { "" } else { "," }
    file.add(x + comma)
  }
}
