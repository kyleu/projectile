package com.kyleu.projectile.models.web

import better.files.Resource
import play.twirl.api.Html

import scala.util.Random

object StartupErrorFixes {
  private[this] def sqlError(error: String, sql: String) = {
    val m = s"<p>$error run the following sql to resolve the issue:</p>"
    val preId = Random.alphanumeric.take(5).mkString
    val sqlContent = s"""<a href="" onclick="$$('#$preId').toggle();return false;">Show SQL</a><pre id="$preId" style="display: none;">$sql</pre>"""
    Html(m + sqlContent)
  }

  def tableError(table: String) = {
    val schema = table match {
      case "audit" => "AuditSchema"
      case "note" => "NoteSchema"
      case "system_user" => "SystemUserSchema"
      case "scheduled_task_run" => "ScheduledTaskRunSchema"
      case _ => ""
    }
    if (schema.isEmpty) {
      Html("<p>See the error logs for more information</p>")
    } else {
      sqlError(s"""The "$table" table doesn't exist. You can""", Resource.getAsString(s"ddl/$schema.sql"))
    }
  }

  def messageFor(key: String, msg: String, params: Map[String, String]) = key match {
    case "database" => msg match {
      case _ if msg.contains("Connection") && msg.contains("refused") =>
        Html("Make sure the server is running, or change the database settings in application.conf")
      case _ if msg.contains("role") && msg.contains("does not exist") =>
        val u = params.getOrElse("username", "{{USERNAME}}")
        val sql = s"create user $u with login nosuperuser createdb nocreaterole inherit noreplication connection limit -1 password '$u';"
        sqlError("The user doesn't exist. Either set <code>database.application.local.username</code> in application.conf, or", sql)
      case _ if msg.contains("database") && msg.contains("does not exist") =>
        val u = params.getOrElse("username", "{{USERNAME}}")
        val db = params.getOrElse("database", "{{DATABASE}}")
        val sql = s"create database $db with owner = $u encoding = 'utf8' connection limit = -1;"
        sqlError("The database doesn't exist. Either set <code>database.application.local.database</code> in application.conf, or", sql)
      case _ => Html("<p>See the error logs for more information</p>")
    }
    case _ if key.startsWith("table.") => tableError(key.stripPrefix("table."))
    case "app" => msg match {
      case _ if msg.contains("Database not initialized") => Html("<p>This may have been caused by an earlier error</p>")
      case _ => Html("<p>See the error logs for more information</p>")
    }
    case _ => Html(s"<p>Unknown error [$key]</p>")
  }
}
