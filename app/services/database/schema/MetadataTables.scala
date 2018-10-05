package services.database.schema

import java.sql.{Connection, DatabaseMetaData}

import models.database.query.Query
import models.database.schema.{EnumType, Table}
import services.database.query.{JdbcHelper, JdbcRow, QueryExecutor}
import util.{Logging, NullUtils}

import scala.util.control.NonFatal

object MetadataTables extends Logging {
  def getTables(metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String]) = {
    val rs = metadata.getTables(catalog.orNull, schema.orNull, NullUtils.inst, Array("TABLE"))
    new JdbcRow.Iter(rs).map(row => fromRow(row)).toList.sortBy(_.name)
  }

  def withTableDetails(conn: Connection, metadata: DatabaseMetaData, tables: Seq[Table], enums: Seq[EnumType]) = {
    tables.zipWithIndex.map { table =>
      if (table._2 > 0 && table._2 % 25 == 0) { log.info(s"Processed [${table._2}/${tables.size}] tables...") }
      getTableDetails(conn, metadata, table._1, enums)
    }
  }

  private[this] def getTableDetails(conn: Connection, metadata: DatabaseMetaData, table: Table, enums: Seq[EnumType]) = try {
    val rowStats = new QueryExecutor(conn)(new Query[Option[(String, Option[String], Long, Option[Int], Option[Long], Option[Long])]] {
      val t = s"""${table.schema.fold("")(_ + ".")}"${table.name}""""
      override val sql = s"select relname as name, reltuples as rows from pg_class where oid = '$t'::regclass"
      override def reduce(rows: Iterator[JdbcRow]) = rows.map { row =>
        val tableName = row.as[String]("name")
        val rowEstimate = JdbcHelper.longVal(row.as[Any]("rows"))

        (tableName, None, rowEstimate, None, None, None)
      }.toList.headOption
    })

    table.copy(
      definition = None,
      storageEngine = rowStats.flatMap(_._2),
      rowCountEstimate = rowStats.map(_._3),
      averageRowLength = rowStats.flatMap(_._4),
      dataLength = rowStats.flatMap(_._5),
      columns = MetadataColumns.getColumns(metadata, table.catalog, table.schema, table.name, enums),
      rowIdentifier = MetadataIdentifiers.getRowIdentifier(metadata, table.catalog, table.schema, table.name),
      primaryKey = MetadataKeys.getPrimaryKey(metadata, table),
      foreignKeys = MetadataKeys.getForeignKeys(metadata, table),
      indexes = MetadataIndexes.getIndexes(metadata, table),
      createTime = rowStats.flatMap(_._6)
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
