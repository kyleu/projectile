package com.kyleu.projectile.services.database.schema

import java.sql.{Connection, DatabaseMetaData}

import com.kyleu.projectile.models.database.schema.{EnumType, Table}
import com.kyleu.projectile.services.database.query.JdbcRow
import com.kyleu.projectile.util.{Logging, NullUtils}

import scala.util.control.NonFatal

object MetadataTables extends Logging {
  def getTables(metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String]) = {
    val rs = metadata.getTables(catalog.orNull, schema.orNull, NullUtils.inst, Array("TABLE"))
    new JdbcRow.Iter(rs).map(row => fromRow(row)).toList.sortBy(_.name)
  }

  def withTableDetails(conn: Connection, metadata: DatabaseMetaData, tables: Seq[Table], enums: Seq[EnumType]) = if (tables.isEmpty) {
    Nil
  } else {
    val startMs = System.currentTimeMillis
    log.info(s"Loading [${tables.size}] tables...")
    val ret = tables.zipWithIndex.map { table =>
      if (table._2 > 0 && table._2 % 10 == 0) { log.info(s"Processed [${table._2}/${tables.size}] tables...") }
      getTableDetails(conn, metadata, table._1, enums)
    }
    log.info(s"[${tables.size}] tables loaded in [${System.currentTimeMillis - startMs}ms]")
    ret
  }

  private[this] def getTableDetails(conn: Connection, metadata: DatabaseMetaData, table: Table, enums: Seq[EnumType]) = try {
    table.copy(
      definition = None,
      columns = MetadataColumns.getColumns(metadata, table.catalog, table.schema, table.name, enums),
      rowIdentifier = MetadataIdentifiers.getRowIdentifier(metadata, table.catalog, table.schema, table.name),
      primaryKey = MetadataKeys.getPrimaryKey(metadata, table),
      foreignKeys = MetadataKeys.getForeignKeys(metadata, table),
      indexes = MetadataIndexes.getIndexes(metadata, table)
    )
  } catch {
    case NonFatal(x) =>
      log.warn(s"Unable to get table details for [${table.name}]", x)
      table
  }

  private[this] def fromRow(row: JdbcRow) = Table(
    name = row.as[String]("TABLE_NAME"),
    catalog = row.asOpt[String]("TABLE_CAT"),
    schema = row.asOpt[String]("TABLE_SCHEM"),
    description = row.asOpt[String]("REMARKS"),
    definition = try {
      row.asOpt[String]("SQL")
    } catch {
      case NonFatal(_) => None
    }
  )
}
