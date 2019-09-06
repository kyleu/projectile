package com.kyleu.projectile.controllers.admin.sql

import java.io.ByteArrayOutputStream

import com.github.tototoshi.csv.CSVWriter
import com.kyleu.projectile.controllers.{AuthController, BaseController}
import com.kyleu.projectile.models.database.jdbc.JdbcRow
import com.kyleu.projectile.models.database.{ListQuery, Query, Row}
import com.kyleu.projectile.models.module.Application
import com.kyleu.projectile.services.database.JdbcDatabase
import com.kyleu.projectile.util.tracing.TraceData

import scala.concurrent.ExecutionContext

@javax.inject.Singleton
class SqlTableController @javax.inject.Inject() (
    override val app: Application, db: JdbcDatabase
)(implicit ec: ExecutionContext) extends AuthController("tables") {
  def list = withSession("tables", ("tools", "SQL", "export")) { implicit request => implicit td =>
    val cfg = app.cfg(u = Some(request.identity), "system", "tools", "sql")
    db.queryF(new ListQuery[String] {
      override def sql = "select tablename from pg_catalog.pg_tables where schemaname != 'pg_catalog' and schemaname != 'information_schema';"
      override def map(row: Row) = row.as[String]("tablename")
    }).map { names =>
      Ok(com.kyleu.projectile.views.html.admin.sql.tableList(cfg, names))
    }
  }

  def export(table: String) = withSession("export", ("tools", "SQL", "export")) { _ => implicit td =>
    exportTable(table).map { r =>
      Ok(r).as(BaseController.MimeTypes.csv).withHeaders("Content-Disposition" -> s"""inline; filename="${table}-export.csv"""")
    }
  }

  private[this] def exportTable(table: String)(implicit td: TraceData) = {
    val os = new ByteArrayOutputStream()
    val writer = CSVWriter.open(os)

    writer.writeRow(Seq(s"Projectile Export of table [$table]"))

    var processedFields = false

    db.queryF(new Query[Unit] {
      override def sql = s"""select * from "$table";"""
      override def reduce(rows: Iterator[Row]) = rows.map {
        case j: JdbcRow =>
          if (!processedFields) {
            writer.writeRow(j.colNames)
            processedFields = true
          }
          writer.writeRow(j.toSeq.map(_.orNull))
      }.toList
    }).map(_ => new String(os.toByteArray))
  }
}
