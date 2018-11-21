package com.projectile.models.output.feature.core

import com.projectile.models.database.schema.ColumnType
import com.projectile.models.export.ExportModel
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.feature.ModelFeature
import com.projectile.models.output.file.ScalaFile

object ModelHelper {
  def addFields(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = model.fields.foreach { field =>
    field.addImport(config, file, model.modelPackage)

    val scalaJsPrefix = if (model.features(ModelFeature.ScalaJS)) { "@JSExport " } else { "" }

    val colScala = field.t match {
      case ColumnType.ArrayType => ColumnType.ArrayType.valForSqlType(field.sqlTypeName)
      case ColumnType.TagsType =>
        file.addImport(config.tagsPackage, "Tag")
        field.scalaType(config)
      case _ => field.scalaType(config)
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
        case x if x.requiredImport.contains("java.time") => file.addImport(config.utilitiesPackage, "DateUtils")
        case _ => // noop
      }

      val colScala = field.t match {
        case ColumnType.ArrayType => ColumnType.ArrayType.valForSqlType(field.sqlTypeName)
        case _ => field.scalaType(config)
      }
      val propType = if (field.notNull) { colScala } else { "Option[" + colScala + "]" }
      val propDefault = if (field.notNull) {
        " = " + field.defaultString(config)
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
