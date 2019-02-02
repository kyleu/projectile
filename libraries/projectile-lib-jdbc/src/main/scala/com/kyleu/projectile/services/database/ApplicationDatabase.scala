package com.kyleu.projectile.services.database

import com.kyleu.projectile.util.tracing.TraceData

import scala.util.control.NonFatal

/** The database used for all application models */
object ApplicationDatabase extends JdbcDatabase("application", "database.application") {
  def migrate()(implicit td: TraceData) = MigrateTask.migrate(source)

  def migrateSafe() = try {
    migrate()(TraceData.noop)
  } catch {
    case NonFatal(x) => log.error(s"Unable to process Flyway migrations: $x", x)(TraceData.noop)
  }
}
