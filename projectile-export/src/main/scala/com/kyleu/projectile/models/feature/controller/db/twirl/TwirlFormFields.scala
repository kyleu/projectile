package com.kyleu.projectile.models.feature.controller.db.twirl

import com.kyleu.projectile.models.database.schema.ForeignKey
import com.kyleu.projectile.models.export.{ExportField, ExportModel}
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.{FieldType, FieldTypeFromString}
import com.kyleu.projectile.models.output.CommonImportHelper
import com.kyleu.projectile.models.output.file.OutputFile

object TwirlFormFields {
  def fieldFor(
    config: ExportConfiguration,
    model: ExportModel,
    field: ExportField,
    file: OutputFile,
    autocomplete: Option[(ForeignKey, ExportModel)],
    isNewOverride: Option[String] = None,
    vOverride: Option[String] = None
  ) = {
    val formPkg = (config.systemViewPackage ++ Seq("html", "components", "form")).mkString(".")
    val isNew = isNewOverride.getOrElse(if (model.pkFields.contains(field) || field.required) { "isNew" } else { "false" })

    lazy val args = getArgs(config, model, field, isNew, vOverride, None)
    lazy val argsString = getArgs(config, model, field, isNew, vOverride, Some(".toString"))
    lazy val argsMapped = getArgs(config, model, field, isNew, vOverride, Some(".map(_.toString)"))

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

      case _ if autocomplete.isDefined =>
        val ac = autocomplete.getOrElse(throw new IllegalStateException())
        val acArgs = getArgs(config, model, field, isNew, vOverride, Some(".toString"), ac = true)
        val formPkg = (config.systemViewPackage ++ Seq("html", "components", "form")).mkString(".")
        file.add(s"@$formPkg.autocompleteField(", 1)
        file.add(s"""$acArgs, dataType = "${field.t}",""")
        val url = s"${TwirlHelper.routesClass(config, ac._2)}.autocomplete()"
        file.add(s"""call = $url, acType = ("${ac._2.propertyName}", "${ac._2.title}"), icon = ${ac._2.iconRef(config)},""")
        file.add(s"""inputType = "${inputType(field.t)}"""")
        file.add(")", -1)

      case FieldType.ByteType => file.add(s"@$formPkg.byteField($args)")
      case FieldType.ByteArrayType => file.add(s"@$formPkg.byteArrayField($args)")
      case FieldType.BooleanType => file.add(s"@$formPkg.booleanField($args)")
      case FieldType.BigDecimalType => file.add(s"@$formPkg.decimalField($args)")
      case FieldType.DoubleType => file.add(s"@$formPkg.doubleField($args)")
      case FieldType.LongType => file.add(s"@$formPkg.longField($args)")
      case FieldType.IntegerType => file.add(s"@$formPkg.intField($args)")
      case FieldType.UuidType => file.add(s"@$formPkg.uuidField($args)")

      case FieldType.DateType => file.add(s"@$formPkg.localDateField($args)")
      case FieldType.TimeType => file.add(s"@$formPkg.localTimeField($args)")
      case FieldType.TimestampType => file.add(s"@$formPkg.localDateTimeField($args)")
      case FieldType.TimestampZonedType => file.add(s"@$formPkg.zonedDateTimeField($args)")

      case _ =>
        file.add(s"""@$formPkg.textField($argsString, dataType = "${field.t}")""")
    }
  }

  private[this] def getArgs(
    config: ExportConfiguration, model: ExportModel, field: ExportField,
    isNew: String, vOverride: Option[String], extra: Option[String], ac: Boolean = false
  ) = {
    val prop = field.propertyName
    val v = if (field.required) { s"Some(model.$prop${extra.getOrElse("")})" } else { s"model.$prop${extra.map(".map(_" + _ + ")").getOrElse("")}" }
    val va = vOverride match {
      case Some(o) => o
      case None if model.foreignKeys.exists(_.references.exists(_.source == field.key)) => s"if(isNew) { None } else { $v }"
      case None => v
    }
    val tx = FieldTypeFromString.fromString(config, field.t, "s") match {
      case _ if ac => ""
      case "s" => ""
      case _ if field.t.isInstanceOf[FieldType.EnumType] => ""
      case x => s".map(s => $x)".replaceAllLiterally(
        literal = "DateUtils", replacement = CommonImportHelper.getString(config, "DateUtils")
      ).replaceAllLiterally(
        literal = "UUID", replacement = "java.util.UUID"
      ).replaceAllLiterally(
        literal = "util.JsonSerializers.toJson(s)", replacement = CommonImportHelper.getString(config, "JsonSerializers") + ".readJson(s)"
      ).replaceAllLiterally(
        literal = "Tag.", replacement = CommonImportHelper.getString(config, "Tag") + "."
      ).replaceAllLiterally(
        literal = "StringUtils", replacement = CommonImportHelper.getString(config, "StringUtils")
      )
    }
    val value = s"""request.queryString.get("$prop").flatMap(_.headOption)$tx.orElse($va)"""
    val selected = s"""request.queryString.isDefinedAt("$prop") || $isNew""".stripSuffix(" || false")
    val pk = if (model.pkFields.contains(field)) { ", isPk = true" } else { "" }
    s"""selected = $selected, key = "$prop", title = "${field.title}", value = $value, nullable = ${field.optional}$pk"""
  }

  private[this] def inputType(t: FieldType) = t match {
    case FieldType.IntegerType | FieldType.LongType => "number"
    case _ => "text"
  }
}
