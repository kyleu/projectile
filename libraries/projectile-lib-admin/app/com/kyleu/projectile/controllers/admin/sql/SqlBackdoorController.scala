package com.kyleu.projectile.controllers.admin.sql

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.controllers.admin.sql.routes.SqlBackdoorController
import com.kyleu.projectile.models.database.{Query, Row}
import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.module.ApplicationFeature.Sql.value
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.queries.SqlParser
import com.kyleu.projectile.models.web.InternalIcons
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.database.JdbcDatabase
import com.kyleu.projectile.services.sql.SqlExecutionHelper

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@javax.inject.Singleton
class SqlBackdoorController @javax.inject.Inject() (
    override val app: Application, db: JdbcDatabase
)(implicit ec: ExecutionContext) extends AuthController("sql") {
  ApplicationFeature.enable(ApplicationFeature.Sql)
  PermissionService.registerModel("tools", "SQL", "SQL Access", Some(InternalIcons.sql), "prompt", "commit", "export", "import")
  val desc = "A SQL prompt for the application database (dangerous)"
  SystemMenu.addToolMenu(value, "SQL Access", Some(desc), SqlBackdoorController.sql(), InternalIcons.sql, ("tools", "SQL", "prompt"))

  def sql(t: Option[String]) = withSession("form", ("tools", "SQL", "prompt")) { implicit request => implicit td =>
    val cfg = app.cfg(u = Some(request.identity), "system", "tools", "sql")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.sql.sqlForm(cfg, s"select * from ${t.getOrElse("foo")}", None)))
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
          SqlExecutionHelper.executeUnknown(conn, q, None) match {
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
        case "csv" => Ok(SqlExecutionHelper.csvFor(
          records = results.head._4, fields = results.head._3
        ).toString).withHeaders(CONTENT_DISPOSITION -> "attachment; filename=fuchu-export.csv")
        case _ => throw new IllegalStateException("Can only handle \"html\" and \"csv\" formats")
      }
      if ((!commit) || (!PermissionService.check(request.identity.role, "tools", "SQL", "commit")._1)) { conn.rollback() }
      Future.successful(response)
    }
  }
}
