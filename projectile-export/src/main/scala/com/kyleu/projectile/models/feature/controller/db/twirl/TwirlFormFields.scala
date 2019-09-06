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

    val prop = field.propertyName
    def getArgs(extra: Option[String] = None) = {
      val v = if (field.required) { s"Some(model.$prop${extra.getOrElse("")})" } else { s"model.$prop${extra.map(".map(_" + _ + ")").getOrElse("")}" }
      s"""selected = $selected, key = "$prop", title = "${field.title}", value = $v, nullable = ${field.optional}"""
    }

    val args = getArgs()
    val argsString = getArgs(Some(".toString"))
    val argsMapped = getArgs(Some(".map(_.toString)"))

    field.t match {
      case FieldType.StringType => file.add(s"@$formPkg.textField($args)")

      case FieldType.EnumType(key) =>
        val enum = config.getEnumOpt(key).getOrElse(throw new IllegalStateException(s"Cannot find enum with name [$key]"))
        val opts = "Seq(" + enum.values.map(v => s""""${v.k}" -> "${v.v}"""").mkString(", ") + ")"
        val extra = s"""options = $opts, dataType = "${enum.key}""""
        file.add(s"@$formPkg.selectField($argsString, $extra)")

      case FieldType.CodeType => file.add(s"@$formPkg.codeField($args)")
      case FieldType.JsonType => file.add(s"@$formPkg.jsonField($args)")

      case FieldType.ListType(t) if t == FieldType.StringType => file.add(s"@$formPkg.collectionField($args)")
      case FieldType.ListType(_) => file.add(s"@$formPkg.collectionField($argsMapped)")
      case FieldType.SetType(t) if t == FieldType.StringType => file.add(s"""@$formPkg.collectionField($args, dataType="set")""")
      case FieldType.SetType(_) => file.add(s"""@$formPkg.collectionField($argsMapped, dataType="set")""")

      case FieldType.MapType(_, _) => file.add(s"@$formPkg.mapField($args)")
      case FieldType.TagsType => file.add(s"@$formPkg.tagsField($args)")

      case FieldType.BooleanType => file.add(s"@$formPkg.booleanField($args)")

      case FieldType.DateType => file.add(s"@$formPkg.localDateField($args)")
      case FieldType.TimeType => file.add(s"@$formPkg.localTimeField($args)")
      case FieldType.TimestampType => file.add(s"@$formPkg.localDateTimeField($args)")
      case FieldType.TimestampZonedType => file.add(s"@$formPkg.zonedDateTimeField($args)")

      case _ if autocomplete.isDefined =>
        val ac = autocomplete.getOrElse(throw new IllegalStateException())
        val formPkg = (config.systemViewPackage ++ Seq("html", "components", "form")).mkString(".")
        file.add(s"@$formPkg.autocompleteField(", 1)
        file.add(s"""$argsString, dataType = "${field.t}",""")
        val url = s"${TwirlHelper.routesClass(config, ac._2)}.autocomplete()"
        file.add(s"""call = $url, acType = ("${ac._2.propertyName}", "${ac._2.title}"), icon = ${ac._2.iconRef(config)}""")
        file.add(")", -1)

      case _ =>
        file.add(s"""@$formPkg.textField($argsString, dataType = "${field.t}")""")
    }
  }
}
