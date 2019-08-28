package com.kyleu.projectile.models.export.config

import com.kyleu.projectile.models.database.schema.Column
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.export.{ExportEnum, ExportField}
import com.kyleu.projectile.models.output.ExportHelper.{toDefaultTitle, toIdentifier}

object ExportConfigurationDefault {
  private[this] def clean(str: String) = str match {
    case "type" => "typ"
    case _ => str
  }

  def loadDefault(t: FieldType, v: String) = t match {
    case FieldType.TimestampType if v == "now()" => None
    case FieldType.BooleanType if v == "true" || v == "false" => Some(v)
    case FieldType.BooleanType => throw new IllegalStateException(s"Unknown default value [$v] for boolean")
    case FieldType.StringType => Some(v.indexOf("::char") match {
      case -1 => v.stripSuffix("'").stripPrefix("'")
      case idx => v.substring(0, idx).stripSuffix("'").stripPrefix("'")
    })
    case _ => Some(v)
  }

  def loadField(col: Column, indexed: Boolean, unique: Boolean, inSearch: Boolean = false, inSummary: Boolean = false, enums: Seq[ExportEnum]) = ExportField(
    key = col.name,
    propertyName = clean(toIdentifier(col.name)),
    title = toDefaultTitle(col.name),
    description = col.description,
    t = col.columnType,
    defaultValue = col.defaultValue.flatMap(loadDefault(col.columnType, _)),
    required = col.notNull,
    indexed = indexed,
    unique = unique,
    inSearch = inSearch,
    inSummary = inSummary
  )
}
