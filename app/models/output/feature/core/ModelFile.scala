package models.output.feature.core

import models.database.schema.ColumnType
import models.export.ExportModel
import models.export.config.ExportConfiguration
import models.output.file.ScalaFile
import services.output.OutputPath

object ModelFile {
  val includeDefaults = false

  def export(config: ExportConfiguration, model: ExportModel) = {
    val path = OutputPath.ServerSource
    val file = ScalaFile(path = path, dir = model.pkg, key = model.className)

    file.addImport("models.result.data", "DataField")
    file.addImport("models.result.data", "DataSummary")
    file.addImport("models.result.data", "DataFieldModel")
    file.addImport("util.JsonSerializers", "_")
    if (model.scalaJs) {
      file.addImport("scala.scalajs.js.annotation", "JSExport")
      file.addImport("scala.scalajs.js.annotation", "JSExportTopLevel")
    }

    file.add(s"object ${model.className} {", 1)
    file.add(s"implicit val jsonEncoder: Encoder[${model.className}] = deriveEncoder")
    file.add(s"implicit val jsonDecoder: Decoder[${model.className}] = deriveDecoder")
    file.add()
    ModelHelper.addEmpty("", model, file)
    file.add("}", -1)
    file.add()

    model.description.foreach(d => file.add(s"/** $d */"))

    if (model.scalaJs) {
      file.add(s"""@JSExportTopLevel(util.Config.projectId + ".${model.className}")""")
    }
    file.add(s"final case class ${model.className}(", 2)
    ModelHelper.addFields("", model, file)
    model.extendsClass match {
      case Some(x) => file.add(") extends " + x + " {", -2)
      case None => file.add(") extends DataFieldModel {", -2)
    }
    file.indent()
    file.add("override def toDataFields = Seq(", 1)
    model.fields.foreach { field =>
      val x = if (field.notNull) {
        val method = field.t match {
          case ColumnType.StringType | ColumnType.EncryptedStringType => field.propertyName
          case ColumnType.EnumType => s"${field.propertyName}.value"
          case ColumnType.ArrayType => s""""{ " + ${field.propertyName}.mkString(", ") + " }""""
          case _ => s"${field.propertyName}.toString"
        }
        s"""DataField("${field.propertyName}", Some($method))"""
      } else {
        val method = field.t match {
          case ColumnType.StringType | ColumnType.EncryptedStringType => field.propertyName
          case ColumnType.EnumType => s"${field.propertyName}.map(_.value)"
          case ColumnType.ArrayType => s"""${field.propertyName}.map(v => "{ " + v.mkString(", ") + " }")"""
          case _ => s"${field.propertyName}.map(_.toString)"
        }
        s"""DataField("${field.propertyName}", $method)"""
      }
      val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
      file.add(x + comma)
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
    file
  }
}
