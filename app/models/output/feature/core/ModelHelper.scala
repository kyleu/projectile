package models.output.feature.core

import models.database.schema.ColumnType
import models.export.ExportModel
import models.export.config.ExportConfiguration
import models.output.feature.Feature
import models.output.file.ScalaFile

object ModelHelper {
  def addFields(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = model.fields.foreach { field =>
    field.addImport(file, model.modelPackage)

    val scalaJsPrefix = if (model.features(Feature.ScalaJS)) { "@JSExport " } else { "" }

    val colScala = field.t match {
      case ColumnType.ArrayType => ColumnType.ArrayType.valForSqlType(field.sqlTypeName)
      case ColumnType.TagsType =>
        file.addImport(config.tagsPackage.mkString("."), "Tag")
        field.scalaType
      case _ => field.scalaType
    }
    val propType = if (field.notNull) { colScala } else { "Option[" + colScala + "]" }
    val propDecl = s"$scalaJsPrefix${field.propertyName}: $propType"
    val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
    field.description.foreach(d => file.add("/** " + d + " */"))
    file.add(propDecl + comma)
  }

  def addEmpty(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val fieldStrings = model.fields.map { field =>
      field.t match {
        case x if x.requiredImport.contains("java.time") => file.addImport(config.utilitiesPackage.mkString("."), "DateUtils")
        case _ => // noop
      }

      val colScala = field.t match {
        case ColumnType.ArrayType => ColumnType.ArrayType.valForSqlType(field.sqlTypeName)
        case _ => field.scalaType
      }
      val propType = if (field.notNull) { colScala } else { "Option[" + colScala + "]" }
      val propDefault = if (field.notNull) {
        " = " + field.defaultString
      } else {
        " = None"
      }
      s"${field.propertyName}: $propType$propDefault"
    }.mkString(", ")
    file.add(s"def empty($fieldStrings) = {", 1)
    file.add(s"${model.className}(${model.fields.map(_.propertyName).mkString(", ")})")
    file.add("}", -1)
  }
}
