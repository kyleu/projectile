package com.kyleu.projectile.models.module

import com.kyleu.projectile.models.database.DatabaseConfig

import scala.util.control.NonFatal

class ApplicationErrors(app: Application) {
  private[this] var errors = List.empty[(String, String, Map[String, String], Option[Throwable])]
  private[this] var tables = Set.empty[String]

  def addError(key: String, msg: String, params: Map[String, String] = Map.empty, ex: Option[Throwable] = None) = {
    errors = errors :+ ((key, msg, params, ex))
  }
  def hasErrors = errors.nonEmpty
  def getErrors = errors

  def checkDatabase() = if (!app.db.isStarted) {
    try {
      app.db.open(app.config.cnf.underlying, app.tracing)
    } catch {
      case NonFatal(x) =>
        val c = DatabaseConfig.fromConfig(app.config.cnf.underlying, "database.application")
        val params = Map("username" -> c.username, "database" -> c.database.getOrElse(""))
        addError("database", s"Cannot connect to [${c.database.getOrElse("")}/${c.host}]: ${x.getMessage}", params, Some(x))
    }
  }

  def checkTable(name: String) = {
    tables = tables + name
    if (!app.db.doesTableExist(name)) { addError("table." + name, "Missing [" + name + "] table") }
  }
  def checkTables() = tables.foreach(checkTable)

  def clear() = {
    errors = Nil
    tables.foreach(checkTable)
  }
}
