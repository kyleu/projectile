package com.kyleu.projectile.models.database.input

import com.kyleu.projectile.models.database.schema.{ProvidedModels, Table}
import com.kyleu.projectile.models.export.config.{ExportConfigurationDefault, ExportConfigurationHelper}
import com.kyleu.projectile.models.export.{ExportEnum, ExportModel}
import com.kyleu.projectile.models.output.ExportHelper.{toClassName, toDefaultTitle, toIdentifier}
import com.kyleu.projectile.models.input.InputType
import com.kyleu.projectile.models.output.ExportHelper

object TableExportModel {
  def loadTableModel(t: Table, tables: Seq[Table], enums: Seq[ExportEnum]) = {
    val cn = PostgresInput.rowName(toClassName(t.name))
    val title = toDefaultTitle(toClassName(t.name))

    ExportModel(
      inputType = InputType.Model.PostgresTable,
      key = t.name,
      pkg = Nil,
      propertyName = toIdentifier(cn),
      className = cn,
      title = title,
      description = t.description,
      plural = ExportHelper.toDefaultPlural(title),
      fields = loadTableFields(t, enums),
      pkColumns = ExportConfigurationHelper.pkColumns(t),
      foreignKeys = t.foreignKeys.groupBy(x => x.references).map(_._2.headOption.getOrElse(throw new IllegalStateException())).toList,
      references = ExportConfigurationHelper.references(tables, t, Map.empty),
      provided = ProvidedModels.models.isDefinedAt(t.name)
    )
  }

  private[this] def loadTableFields(t: Table, enums: Seq[ExportEnum]) = t.columns.toList.map { col =>
    val inPk = t.primaryKey.exists(_.columns.contains(col.name))
    val idxs = t.indexes.filter(i => i.columns.exists(_.name == col.name)).map(i => i.name -> i.unique)
    val inIndex = idxs.nonEmpty
    val unique = idxs.exists(_._2)
    val inGlobalSearch = inPk
    val inLocalSearch = inPk || inIndex
    val inSummary = inPk || inIndex
    ExportConfigurationDefault.loadField(
      col = col, indexed = inIndex, unique = unique, inGlobalSearch = inGlobalSearch, inLocalSearch = inLocalSearch, inSummary = inSummary, enums = enums
    )
  }
}
