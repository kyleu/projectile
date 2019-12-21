package com.kyleu.projectile.models.module

import com.kyleu.projectile.models.database.DatabaseConfig
import com.kyleu.projectile.util.tracing.TraceData

import scala.util.control.NonFatal

object ApplicationErrors {
  final case class Err(key: String, msg: String, params: Map[String, String] = Map.empty, ex: Option[Throwable] = None)
}

class ApplicationErrors(app: Application) {
  private[this] var errors = List.empty[ApplicationErrors.Err]
  private[this] var tables = Set.empty[String]

  def addError(key: String, msg: String, params: Map[String, String] = Map.empty, ex: Option[Throwable] = None) = {
    errors = errors :+ ApplicationErrors.Err(key, msg, params, ex)
  }
  def hasErrors = errors.nonEmpty

  private[this] def idx(k: String) = k match {
    case "table.system_user" => 0
    case "table.audit" => 1
    case _ => 10000
  }

  def getErrors = errors.map(e => idx(e.key) -> e).sortBy(_._1).map(_._2)

  def checkDatabase() = {
    if (!app.db.isStarted) {
      try {
        app.db.open(app.config.cnf.underlying, app.tracing)
      } catch {
        case NonFatal(x) =>
          val c = DatabaseConfig.fromConfig(app.config.cnf.underlying, "database.application")
          val params = Map("username" -> c.username, "database" -> c.database.getOrElse(""))
          val msg = s"Cannot connect to application database at [${c.database.getOrElse("")}/${c.host}]: ${x.getMessage}"
          addError("database", msg, params, Some(x))
      }
    }
    if (!app.systemDb.isStarted) {
      try {
        app.systemDb.open(app.config.cnf.underlying, app.tracing)
      } catch {
        case NonFatal(x) =>
          val c = DatabaseConfig.fromConfig(app.config.cnf.underlying, "database.system")
          val params = Map("username" -> c.username, "database" -> c.database.getOrElse(""))
          val msg = s"Cannot connect to system database at [${c.database.getOrElse("")}/${c.host}]: ${x.getMessage}"
          addError("database", msg, params, Some(x))
      }
    }
  }

  def checkTable(name: String)(implicit td: TraceData = TraceData.noop) = {
    tables = tables + name
    if (!app.systemDb.doesTableExist(name)) { addError("table." + name, "Missing [" + name + "] table") }
  }
  def checkTables(implicit td: TraceData) = tables.foreach(checkTable)

  def clear(implicit td: TraceData) = {
    errors = Nil
    tables.foreach(checkTable)
  }
}
