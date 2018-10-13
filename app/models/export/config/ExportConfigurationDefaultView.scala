package models.export.config

import models.output.ExportHelper.{toClassName, toDefaultTitle, toIdentifier}
import models.database.schema._
import models.export.{ExportEnum, ExportModel}

object ExportConfigurationDefaultView {
  def loadViewModel(v: View, enums: Seq[ExportEnum]) = {
    val cn = toClassName(v.name)
    val title = toDefaultTitle(cn)
    val plural = title + "s"

    ExportModel(
      tableName = v.name,
      pkg = Nil,
      propertyName = toIdentifier(cn),
      className = cn,
      title = title,
      description = v.description,
      plural = plural,
      fields = loadViewFields(v, enums),
      pkColumns = Nil,
      foreignKeys = Nil,
      references = Nil
    )
  }

  private[this] def loadViewFields(v: View, enums: Seq[ExportEnum]) = v.columns.zipWithIndex.toList.map { col =>
    ExportConfigurationDefault.loadField(col = col._1, idx = col._2, indexed = false, unique = false, enums = enums)
  }
}
