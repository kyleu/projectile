package com.kyleu.projectile.models.web

import better.files.Resource
import com.kyleu.projectile.models.database.Statement
import com.kyleu.projectile.models.module.Application
import com.kyleu.projectile.models.queries.SqlParser
import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.tracing.TraceData
import play.twirl.api.Html

import scala.util.Random
import scala.util.control.NonFatal

object StartupErrorFixes extends Logging {
  def messageFor(key: String, msg: String, params: Map[String, String]) = key match {
    case "database" => msg match {
      case _ if msg.contains("Connection") && msg.contains("refused") =>
        None -> Html("Make sure the server is running, or change the database settings in application.conf")
      case _ if msg.contains("role") && msg.contains("does not exist") =>
        val u = params.getOrElse("username", "{{USERNAME}}")
        val sql = s"create user $u with login nosuperuser createdb nocreaterole inherit noreplication connection limit -1 password '$u';"
        None -> sqlError("The user doesn't exist. Either set <code>database.application.local.username</code> in application.conf, or", sql)
      case _ if msg.contains("database") && msg.contains("does not exist") =>
        val u = params.getOrElse("username", "{{USERNAME}}")
        val db = params.getOrElse("database", "{{DATABASE}}")
        val sql = s"create database $db with owner = $u encoding = 'utf8' connection limit = -1;"
        Some("Create Database") -> sqlError("The database doesn't exist. Either set <code>database.application.local.database</code> in application.conf, or", sql)
      case _ => None -> Html("<p>See the error logs for more information</p>")
    }
    case _ if key.startsWith("table.") => Some("Create Tables") -> tableError(key.stripPrefix("table."))
    case "app" => msg match {
      case _ if msg.contains("Database not initialized") => None -> Html("<p>This may have been caused by an earlier error</p>")
      case _ => None -> Html("<p>See the error logs for more information</p>")
    }
    case _ => None -> Html(s"<p>Unknown error [$key]</p>")
  }

  def fix(app: Application, key: String) = key match {
    case _ if key.startsWith("table.") => schemaFor(key.stripPrefix("table.").trim) match {
      case Some(sql) => applySql(app, sql)
      case None => throw new IllegalStateException(s"Cannot fix missing table [${key.stripPrefix("table.")}]")
    }
    case _ => throw new IllegalStateException(s"Cannot fix [$key]")
  }

  private[this] def schemaFor(key: String) = try {
    Some(Resource.getAsString(s"ddl/$key.sql").dropWhile(_.toInt == 65279 /* UTF8 BOM */ ))
  } catch {
    case NonFatal(x) => None
  }

  private[this] def sqlError(error: String, sql: String) = {
    val m = s"<p>$error run the following sql to resolve the issue:</p>"
    val preId = Random.alphanumeric.take(5).mkString
    val sqlContent = s"""<a href="" onclick="$$('#$preId').toggle();return false;">Show SQL</a><pre id="$preId" style="display: none;">$sql</pre>"""
    Html(m + sqlContent)
  }

  private[this] def tableError(table: String) = schemaFor(table) match {
    case None => Html("<p>See the error logs for more information</p>")
    case Some(schema) => sqlError(s"""The "$table" table doesn't exist. You can""", schema)
  }

  private[this] def applySql(app: Application, sql: String) = {
    implicit val td: TraceData = TraceData.noop
    val statements = SqlParser.split(sql)
    app.db.transaction { (_, conn) =>
      statements.map { s =>
        log.info("Running the following SQL:\n" + s._1)
        app.db.execute(Statement.adhoc(s._1), Some(conn))
      }
    }(td)
  }
}
