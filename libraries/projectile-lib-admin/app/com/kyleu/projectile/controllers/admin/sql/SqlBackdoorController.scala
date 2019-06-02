package com.kyleu.projectile.controllers.admin.sql

import java.io.ByteArrayOutputStream
import java.sql.{Connection, PreparedStatement, Types}
import java.util.UUID

import com.github.tototoshi.csv.CSVWriter
import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.controllers.admin.sql.routes.SqlBackdoorController
import com.kyleu.projectile.models.database.jdbc.Conversions
import com.kyleu.projectile.models.database.{Query, Row}
import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.module.ApplicationFeature.Sql.value
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.queries.SqlParser
import com.kyleu.projectile.models.web.InternalIcons
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.database.JdbcDatabase
import com.kyleu.projectile.util.NullUtils
import com.kyleu.projectile.util.tracing.TraceData

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@javax.inject.Singleton
class SqlBackdoorController @javax.inject.Inject() (
    override val app: Application, db: JdbcDatabase
)(implicit ec: ExecutionContext) extends AuthController("sql") {
  ApplicationFeature.enable(ApplicationFeature.Sql)
  PermissionService.registerModel("tools", "SQL", "SQL Access", Some(InternalIcons.sql), "prompt", "commit")
  SystemMenu.addToolMenu(value, "SQL Access", Some("A SQL prompt for the application database (dangerous)"), SqlBackdoorController.sql(), InternalIcons.sql)

  def sql = withSession("form", ("tools", "SQL", "prompt")) { implicit request => implicit td =>
    val cfg = app.cfg(u = Some(request.identity), "system", "tools", "sql")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.sql.sqlForm(cfg, "select * from foo", None)))
  }

  def sqlPost = withSession("post", ("tools", "SQL", "prompt")) { implicit request => implicit td =>
    val form = request.body.asFormUrlEncoded.get
    val sql = form("sql").head
    val format = form.get("format").flatMap(_.headOption).getOrElse("html")
    val commit = form.get("commit").flatMap(_.headOption).contains("true")
    log.error(s"CUSTOM SQL EXECUTION: User [${request.identity.id}/${request.identity.username}] ran sql [$sql]")
    val statements = SqlParser.split(sql).map(_._1)

    db.transaction { (_, conn) =>
      val results = statements.map { s =>
        val q: Query[Seq[Seq[Option[String]]]] = new Query[Seq[Seq[Option[String]]]] {
          override def name = "adhoc"
          override def sql = s
          override def reduce(rows: Iterator[Row]) = rows.map { row =>
            row.toSeq.map(x => x.map(_.toString))
          }.toList
        }
        try {
          val startMs = System.currentTimeMillis
          executeUnknown(conn, q, None) match {
            case Right(aff) => ((System.currentTimeMillis - startMs).toInt, s, Nil, Seq(Seq(Some(s"$aff rows affected"))))
            case Left((cols, res)) => ((System.currentTimeMillis - startMs).toInt, s, cols, res)
          }
        } catch {
          case NonFatal(ex) => (0, s, Nil, Seq(Seq(Some("Busted!"), Some(ex.getClass.getSimpleName), Some(ex.getMessage))))
        }
      }
      val response = format match {
        case "html" =>
          val cfg = app.cfg(u = Some(request.identity), "system", "tools", "sql")
          Ok(com.kyleu.projectile.views.html.admin.sql.sqlForm(cfg, sql, Some(results)))
        case "csv" if results.size > 1 => throw new IllegalStateException("Cannot export CSV for multiple statements")
        case "csv" => Ok(csvFor(results.head._4, results.head._3).toString).withHeaders(CONTENT_DISPOSITION -> "attachment; filename=fuchu-export.csv")
        case _ => throw new IllegalStateException("Can only handle \"html\" and \"csv\" formats")
      }
      if ((!commit) || (!PermissionService.check(request.identity.role, "tools", "SQL", "commit")._1)) { conn.rollback() }
      Future.successful(response)
    }
  }

  @tailrec
  @SuppressWarnings(Array("AsInstanceOf"))
  private[this] def prepare(stmt: PreparedStatement, values: Seq[Any], index: Int = 1): Unit = {
    if (values.nonEmpty) {
      values.headOption.getOrElse(throw new IllegalStateException()) match {
        case v if NullUtils.isNull(v) => stmt.setNull(index, Types.NULL)

        case Some(x) => stmt.setObject(index, Conversions.convert(x.asInstanceOf[AnyRef]))
        case None => stmt.setNull(index, Types.NULL)

        case v => stmt.setObject(index, Conversions.convert(v.asInstanceOf[AnyRef]))
      }
      prepare(stmt, values.tail, index + 1)
    }
  }

  def executeUnknown[A](connection: Connection, query: Query[A], resultId: Option[UUID]): Either[(Seq[String], A), Int] = {
    val q = new com.kyleu.projectile.models.database.jdbc.Queryable {}
    val actualValues = q.valsForJdbc(connection, query.values)
    log.debug(s"${query.sql} with ${actualValues.mkString("(", ", ", ")")}")(TraceData.noop)
    val stmt = connection.prepareStatement(query.sql)
    try {
      try {
        prepare(stmt, actualValues)
      } catch {
        case NonFatal(x) =>
          log.error(s"Unable to prepare raw query [${query.sql}]", x)(TraceData.noop)
          throw x
      }
      val isResultset = stmt.execute()
      if (isResultset) {
        val res = stmt.getResultSet
        try {
          val md = res.getMetaData
          val columns = (1 to md.getColumnCount).map(i => md.getColumnName(i))
          Left(columns -> query.handle(res))
        } catch {
          case NonFatal(x) =>
            log.error(s"Unable to handle query results for [${query.sql}]", x)(TraceData.noop)
            throw x
        } finally {
          res.close()
        }
      } else {
        Right(stmt.getUpdateCount)
      }
    } finally {
      stmt.close()
    }
  }

  def csvFor(records: Seq[Seq[Option[String]]], fields: Seq[String]) = {
    val os = new ByteArrayOutputStream()
    val writer = CSVWriter.open(os)
    writer.writeRow(fields)
    records.foreach(r => writer.writeRow(r.map {
      case Some(o) => o
      case None => NullUtils.inst
    }))
    new String(os.toByteArray)
  }
}
