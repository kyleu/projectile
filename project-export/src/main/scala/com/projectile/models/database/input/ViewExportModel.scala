package com.projectile.models.database.input

import com.projectile.models.database.schema.View
import com.projectile.models.export.config.ExportConfigurationDefault
import com.projectile.models.export.{ExportEnum, ExportModel}
import com.projectile.models.output.ExportHelper.{toClassName, toDefaultTitle, toIdentifier}
import com.projectile.models.input.InputType

object ViewExportModel {
  def loadViewModel(v: View, enums: Seq[ExportEnum]) = {
    val cn = toClassName(v.name)
    val title = toDefaultTitle(cn)

    ExportModel(
      inputType = InputType.Model.PostgresView,
      key = v.name,
      pkg = Nil,
      propertyName = toIdentifier(cn),
      className = cn,
      title = title,
      description = v.description,
      plural = title + "s",
      arguments = Nil,
      fields = loadViewFields(v, enums),
      pkColumns = Nil,
      foreignKeys = Nil,
      references = Nil
    )
  }

  private[this] def loadViewFields(v: View, enums: Seq[ExportEnum]) = v.columns.toList.map { col =>
    ExportConfigurationDefault.loadField(col = col, indexed = false, unique = false, enums = enums)
  }
}
