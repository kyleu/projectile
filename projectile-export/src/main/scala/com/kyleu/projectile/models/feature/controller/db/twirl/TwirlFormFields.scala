package com.kyleu.projectile.models.feature.controller.db.twirl

import com.kyleu.projectile.models.database.schema.ForeignKey
import com.kyleu.projectile.models.export.{ExportField, ExportModel}
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.output.file.OutputFile

object TwirlFormFields {
  def fieldFor(config: ExportConfiguration, model: ExportModel, field: ExportField, file: OutputFile, autocomplete: Option[(ForeignKey, ExportModel)]) = {
    val formPkg = (config.systemViewPackage ++ Seq("html", "components", "form")).mkString(".")
    val selected = if (model.pkFields.contains(field) || field.required) { "isNew" } else { "false" }
    field.t match {
      case FieldType.EnumType(key) => file.add(s"@$formPkg.selectField(${enumArgsFor(config, field, key, selected)})")
      case FieldType.CodeType => file.add(s"@$formPkg.codeField(${argsFor(field, selected)})")
      case FieldType.BooleanType => file.add(s"@$formPkg.booleanField(${boolArgsFor(field, selected)})")
      case FieldType.DateType => timeField(config, field, file, "Date", selected)
      case FieldType.TimeType => timeField(config, field, file, "Time", selected)
      case FieldType.TimestampType => timeField(config, field, file, "DateTime", selected)
      case FieldType.TimestampZonedType => zonedDateTimeField(config, field, file, selected)
      case _ if autocomplete.isDefined => autocompleteField(config, field, autocomplete.getOrElse(throw new IllegalStateException()), file, selected)
      case _ => file.add(s"@$formPkg.textField(${argsFor(field, selected)})")
    }
  }

  private[this] def argsFor(field: ExportField, selected: String) = {
    val prop = field.propertyName
    val valString = if (field.required) { s"Some(model.$prop.toString)" } else { s"""model.$prop.map(_.toString)""" }
    val dataTypeString = if (field.t == FieldType.StringType) { "" } else { s""", dataType = "${field.t}"""" }
    s"""selected = $selected, key = "$prop", title = "${field.title}", value = $valString, nullable = ${field.optional}$dataTypeString"""
  }

  private[this] def boolArgsFor(field: ExportField, selected: String) = {
    val prop = field.propertyName
    val valString = if (field.required) { s"Some(model.$prop)" } else { s"""model.$prop""" }
    val dataTypeString = if (field.t == FieldType.StringType) { "" } else { s""", dataType = "${field.t}"""" }
    s"""selected = $selected, key = "$prop", title = "${field.title}", value = $valString, nullable = ${field.optional}$dataTypeString"""
  }

  private[this] def enumArgsFor(config: ExportConfiguration, field: ExportField, key: String, selected: String) = {
    val enum = config.getEnumOpt(key).getOrElse(throw new IllegalStateException(s"Cannot find enum with name [$key]"))
    val prop = field.propertyName
    val valString = if (field.required) { s"Some(model.$prop.toString)" } else { s"""model.$prop.map(_.toString)""" }
    val opts = "Seq(" + enum.values.map(v => s""""$v" -> "$v"""").mkString(", ") + ")"
    val extra = s"""options = $opts, nullable = ${field.optional}, dataType = "${enum.key}""""
    s"""selected = $selected, key = "$prop", title = "${field.title}", value = $valString, $extra"""
  }

  private[this] def zonedDateTimeField(config: ExportConfiguration, field: ExportField, file: OutputFile, selected: String) = {
    val formPkg = (config.systemViewPackage ++ Seq("html", "components", "form")).mkString(".")
    val prop = field.propertyName
    val valString = if (field.required) { s"Some(model.$prop)" } else { s"""model.$prop""" }
    val args = s"""selected = $selected, key = "$prop", title = "${field.title}", value = $valString, nullable = ${field.optional}"""
    file.add(s"@$formPkg.zonedDateTimeField($args)")
  }

  private[this] def timeField(config: ExportConfiguration, field: ExportField, file: OutputFile, t: String, selected: String) = {
    val formPkg = (config.systemViewPackage ++ Seq("html", "components", "form")).mkString(".")
    val prop = field.propertyName
    val valString = if (field.required) { s"Some(model.$prop)" } else { s"""model.$prop""" }
    val args = s"""selected = $selected, key = "$prop", title = "${field.title}", value = $valString, nullable = ${field.optional}"""
    file.add(s"@$formPkg.local${t}Field($args)")
  }

  private[this] def autocompleteField(
    config: ExportConfiguration, field: ExportField, autocomplete: (ForeignKey, ExportModel), file: OutputFile, selected: String
  ) = {
    val formPkg = (config.systemViewPackage ++ Seq("html", "components", "form")).mkString(".")
    file.add(s"@$formPkg.autocompleteField(", 1)
    file.add(argsFor(field, selected) + ",")
    val url = s"${TwirlHelper.routesClass(config, autocomplete._2)}.autocomplete()"
    val icon = (config.applicationPackage :+ "models" :+ "template").mkString(".") + s".Icons.${autocomplete._2.propertyName}"
    file.add(s"""call = $url, acType = ("${autocomplete._2.propertyName}", "${autocomplete._2.title}"), icon = $icon""")
    file.add(")", -1)
  }
}
