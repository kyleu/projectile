package com.projectile.services.database.schema

import java.sql.DatabaseMetaData

import com.projectile.models.database.schema.{EnumType, View}
import com.projectile.services.database.query.JdbcRow
import com.projectile.util.{Logging, NullUtils}

import scala.util.control.NonFatal

object MetadataViews extends Logging {
  def getViews(metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String]) = {
    val rs = metadata.getTables(catalog.orNull, schema.orNull, NullUtils.inst, Array("VIEW"))
    new JdbcRow.Iter(rs).map(row => fromRow(row)).toList.sortBy(_.name)
  }

  def withViewDetails(metadata: DatabaseMetaData, views: Seq[View], enums: Seq[EnumType]) = if (views.isEmpty) {
    Nil
  } else {
    val startMs = System.currentTimeMillis
    log.info(s"Loading [${views.size}] views...")
    val ret = views.zipWithIndex.map { view =>
      if (view._2 > 0 && view._2 % 25 == 0) { log.info(s"Processed [${view._2}/${views.size}] views...") }
      getViewDetails(metadata, view._1, enums)
    }
    log.info(s"[${views.size}] views loaded in [${System.currentTimeMillis - startMs}ms]")
    ret
  }

  private[this] def getViewDetails(metadata: DatabaseMetaData, view: View, enums: Seq[EnumType]) = try {
    view.copy(columns = MetadataColumns.getColumns(metadata, view.catalog, view.schema, view.name, enums))
  } catch {
    case NonFatal(x) =>
      log.info(s"Unable to get view details for [${view.name}].", x)
      view
  }

  private[this] def fromRow(row: JdbcRow) = View(
    name = row.as[String]("TABLE_NAME"),
    catalog = row.asOpt[String]("TABLE_CAT"),
    schema = row.asOpt[String]("TABLE_SCHEM"),
    description = row.asOpt[String]("REMARKS"),
    definition = None
  )
}
