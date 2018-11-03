package models.database.input

import models.database.schema.Table
import models.export.config.{ExportConfigurationDefault, ExportConfigurationHelper}
import models.export.{ExportEnum, ExportModel}
import models.output.ExportHelper.{toClassName, toDefaultTitle, toIdentifier}
import models.project.member.ProjectMember.InputType

object TableExportModel {
  def loadTableModel(t: Table, tables: Seq[Table], enums: Seq[ExportEnum]) = {
    val cn = toClassName(t.name)
    val title = toDefaultTitle(cn)

    ExportModel(
      inputType = InputType.PostgresTable,
      key = t.name,
      pkg = Nil,
      propertyName = toIdentifier(cn),
      className = cn,
      title = title,
      description = t.description,
      plural = title + "s",
      fields = loadTableFields(t, enums),
      pkColumns = ExportConfigurationHelper.pkColumns(t),
      foreignKeys = t.foreignKeys.groupBy(x => x.references).map(_._2.head).toList,
      references = ExportConfigurationHelper.references(tables, t, Map.empty)
    )
  }

  private[this] def loadTableFields(t: Table, enums: Seq[ExportEnum]) = t.columns.zipWithIndex.toList.map { col =>
    val banned = t.name match {
      case "audit_record" if col._1.name == "changes" => true
      case _ => false
    }
    val inPk = t.primaryKey.exists(_.columns.contains(col._1.name))
    val idxs = t.indexes.filter(i => i.columns.exists(_.name == col._1.name)).map(i => i.name -> i.unique)
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
    val inSearch = (!banned) && (inPk || inIndex || extras(col._1.name))
    ExportConfigurationDefault.loadField(col._1, col._2, inIndex, unique, inSearch, enums)
  }
}
