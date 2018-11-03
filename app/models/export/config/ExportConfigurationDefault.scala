package models.export.config

import models.database.schema._
import models.export.{ExportEnum, ExportField}
import models.output.ExportHelper.{toDefaultTitle, toIdentifier}

object ExportConfigurationDefault {
  private[this] def clean(str: String) = str match {
    case "type" => "typ"
    case _ => str
  }

  def loadField(col: Column, idx: Int, indexed: Boolean, unique: Boolean, inSearch: Boolean = false, enums: Seq[ExportEnum]) = ExportField(
    key = col.name,
    propertyName = clean(toIdentifier(col.name)),
    title = toDefaultTitle(col.name),
    description = col.description,
    idx = idx,
    t = col.columnType,
    sqlTypeName = col.sqlTypeName,
    defaultValue = col.defaultValue,
    notNull = col.notNull,
    indexed = indexed,
    unique = unique,
    inSearch = inSearch,
    inSummary = inSearch
  )
}
