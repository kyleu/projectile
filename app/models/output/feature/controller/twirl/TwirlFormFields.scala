package models.output.feature.controller.twirl

import models.database.schema.{ColumnType, ForeignKey}
import models.export.{ExportField, ExportModel}
import models.export.config.ExportConfiguration
import models.output.file.OutputFile

object TwirlFormFields {
  def fieldFor(config: ExportConfiguration, model: ExportModel, field: ExportField, file: OutputFile, autocomplete: Option[(ForeignKey, ExportModel)]) = {
    val formPkg = (config.applicationPackage ++ Seq("views", "html", "components", "form")).mkString(".")
    field.t match {
      case ColumnType.EnumType => file.add(s"@$formPkg.selectField(${enumArgsFor(config, field)})")
      case ColumnType.CodeType => file.add(s"@$formPkg.codeField(${argsFor(field)})")
      case ColumnType.BooleanType => file.add(s"@$formPkg.booleanField(${boolArgsFor(field)})")
      case ColumnType.DateType => timeField(config, field, file, "Date")
      case ColumnType.TimeType => timeField(config, field, file, "Time")
      case ColumnType.TimestampType => timeField(config, field, file, "DateTime")
      case ColumnType.TimestampZonedType => zonedDateTimeField(config, field, file)
      case _ if autocomplete.isDefined => autocompleteField(config, field, autocomplete.get, file)
      case _ => file.add(s"@$formPkg.textField(${argsFor(field)})")
    }
  }

  private[this] def argsFor(field: ExportField) = {
    val prop = field.propertyName
    val valString = if (field.notNull) { s"Some(model.$prop.toString)" } else { s"""model.$prop.map(_.toString)""" }
    val dataTypeString = if (field.t == ColumnType.StringType) { "" } else { s""", dataType = "${field.t}"""" }
    s"""selected = isNew, key = "$prop", title = "${field.title}", value = $valString, nullable = ${field.nullable}$dataTypeString"""
  }

  private[this] def boolArgsFor(field: ExportField) = {
    val prop = field.propertyName
    val valString = if (field.notNull) { s"Some(model.$prop)" } else { s"""model.$prop""" }
    val dataTypeString = if (field.t == ColumnType.StringType) { "" } else { s""", dataType = "${field.t}"""" }
    s"""selected = isNew, key = "$prop", title = "${field.title}", value = $valString, nullable = ${field.nullable}$dataTypeString"""
  }

  private[this] def enumArgsFor(config: ExportConfiguration, field: ExportField) = {
    val enum = field.enumOpt(config).getOrElse(throw new IllegalStateException(s"Cannot find enum with name [${field.sqlTypeName}]."))
    val prop = field.propertyName
    val valString = if (field.notNull) { s"Some(model.$prop.toString)" } else { s"""model.$prop.map(_.toString)""" }
    val opts = "Seq(" + enum.values.map(v => s""""$v" -> "$v"""").mkString(", ") + ")"
    s"""selected = isNew, key = "$prop", title = "${field.title}", value = $valString, options = $opts, nullable = ${field.nullable}, dataType = "${enum.key}""""
  }

  private[this] def zonedDateTimeField(config: ExportConfiguration, field: ExportField, file: OutputFile) = {
    val formPkg = (config.applicationPackage ++ Seq("views", "html", "components", "form")).mkString(".")
    val prop = field.propertyName
    val valString = if (field.notNull) { s"Some(model.$prop)" } else { s"""model.$prop""" }
    val args = s"""selected = isNew, key = "$prop", title = "${field.title}", value = $valString, nullable = ${field.nullable}"""
    file.add(s"@$formPkg.zonedDateTimeField($args)")
  }

  private[this] def timeField(config: ExportConfiguration, field: ExportField, file: OutputFile, t: String) = {
    val formPkg = (config.applicationPackage ++ Seq("views", "html", "components", "form")).mkString(".")
    val prop = field.propertyName
    val valString = if (field.notNull) { s"Some(model.$prop)" } else { s"""model.$prop""" }
    val args = s"""selected = isNew, key = "$prop", title = "${field.title}", value = $valString, nullable = ${field.nullable}"""
    file.add(s"@$formPkg.local${t}Field($args)")
  }

  private[this] def autocompleteField(config: ExportConfiguration, field: ExportField, autocomplete: (ForeignKey, ExportModel), file: OutputFile) = {
    val formPkg = (config.applicationPackage ++ Seq("views", "html", "components", "form")).mkString(".")
    file.add(s"@$formPkg.autocompleteField(", 1)
    file.add(argsFor(field) + ",")
    val url = s"${TwirlHelper.routesClass(config, autocomplete._2)}.autocomplete()"
    val icon = (config.applicationPackage :+ "models" :+ "template").mkString(".") + s".Icons.${autocomplete._2.propertyName}"
    file.add(s"""call = $url, acType = ("${autocomplete._2.propertyName}", "${autocomplete._2.title}"), icon = $icon""")
    file.add(")", -1)
  }
}
