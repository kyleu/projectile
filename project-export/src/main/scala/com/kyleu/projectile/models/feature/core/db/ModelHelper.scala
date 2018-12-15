package com.kyleu.projectile.models.feature.core.db

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.{FieldType, FieldTypeImports, FieldTypeRestrictions}
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.output.file.ScalaFile

object ModelHelper {
  def addFields(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = model.fields.foreach { field =>
    field.addImport(config, file, model.modelPackage)

    if (FieldTypeRestrictions.isDate(field.t)) {
      config.addCommonImport(file, "DateUtils")
    }

    val scalaJsPrefix = if (model.features(ModelFeature.ScalaJS)) { "@JSExport " } else { "" }

    val colScala = field.t match {
      case FieldType.TagsType =>
        config.addCommonImport(file, "Tag")
        field.scalaType(config)
      case _ => field.scalaType(config)
    }
    val propType = if (field.required) { colScala } else { "Option[" + colScala + "]" }
    val propDecl = s"$scalaJsPrefix${field.propertyName}: $propType"
    val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
    field.description.foreach(d => file.add("/** " + d + " */"))
    file.add(propDecl + comma)
  }

  def addEmpty(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val fieldStrings = model.fields.map { field =>
      val imports = FieldTypeImports.imports(config, field.t)
      imports.foreach {
        case x if x == Seq("java", "time") => config.addCommonImport(file, "DateUtils")
        case _ => //noop
      }

      val colScala = field.t match {
        case _ => field.scalaType(config)
      }
      val propType = if (field.required) { colScala } else { "Option[" + colScala + "]" }
      val propDefault = if (field.required) {
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
