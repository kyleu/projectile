package com.kyleu.projectile.models.database.input

import com.kyleu.projectile.models.database.schema.Table
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
      arguments = Nil,
      fields = loadTableFields(t, enums),
      pkColumns = ExportConfigurationHelper.pkColumns(t),
      foreignKeys = t.foreignKeys.groupBy(x => x.references).map(_._2.headOption.getOrElse(throw new IllegalStateException())).toList,
      references = ExportConfigurationHelper.references(tables, t, Map.empty)
    )
  }

  private[this] def loadTableFields(t: Table, enums: Seq[ExportEnum]) = t.columns.toList.map { col =>
    val banned = t.name match {
      case "audit_record" if col.name == "changes" => true
      case _ => false
    }
    val inPk = t.primaryKey.exists(_.columns.contains(col.name))
    val idxs = t.indexes.filter(i => i.columns.exists(_.name == col.name)).map(i => i.name -> i.unique)
    val inIndex = idxs.nonEmpty
    val unique = idxs.exists(_._2)
    def extras = t.name match {
      case "audit_record" => Set("changes")
      case "note" => Set("rel_type", "rel_pk", "text", "author", "created")
      case "flyway_schema_history" => Set("installed_rank", "version", "description", "type", "installed_on", "success")
      case "sync_progress" => Set("message", "last_time")
      case "scheduled_task_run" => Set("arguments")
      case _ => Set.empty[String]
    }
    val inSearch = (!banned) && (inPk || inIndex || extras(col.name))
    ExportConfigurationDefault.loadField(col, inIndex, unique, inSearch, enums)
  }
}
