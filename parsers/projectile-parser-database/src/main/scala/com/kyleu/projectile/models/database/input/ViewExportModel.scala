package com.kyleu.projectile.models.database.input

import com.kyleu.projectile.models.database.schema.{ProvidedModels, View}
import com.kyleu.projectile.models.export.config.ExportConfigurationDefault
import com.kyleu.projectile.models.export.{ExportEnum, ExportModel}
import com.kyleu.projectile.models.output.ExportHelper.{toClassName, toDefaultTitle, toIdentifier}
import com.kyleu.projectile.models.input.InputType
import com.kyleu.projectile.models.output.ExportHelper

object ViewExportModel {
  def loadViewModel(v: View, enums: Seq[ExportEnum]) = {
    val cn = PostgresInput.rowName(toClassName(v.name))
    val title = toDefaultTitle(toClassName(v.name))

    ExportModel(
      inputType = InputType.Model.PostgresView,
      key = v.name,
      pkg = Nil,
      propertyName = toIdentifier(cn),
      className = cn,
      title = title,
      description = v.description,
      plural = ExportHelper.toDefaultPlural(title),
      fields = loadViewFields(v, enums),
      pkColumns = Nil,
      foreignKeys = Nil,
      references = Nil,
      provided = ProvidedModels.models.isDefinedAt(v.name)
    )
  }

  private[this] def loadViewFields(v: View, enums: Seq[ExportEnum]) = v.columns.toList.map { col =>
    ExportConfigurationDefault.loadField(col = col, indexed = false, unique = false, enums = enums)
  }
}
