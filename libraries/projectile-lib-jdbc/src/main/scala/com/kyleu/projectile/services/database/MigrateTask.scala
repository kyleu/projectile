package com.kyleu.projectile.services.database

import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.tracing.TraceData
import javax.sql.DataSource
import org.flywaydb.core.Flyway

import scala.util.control.NonFatal

object MigrateTask extends Logging {
  def migrate(source: DataSource)(implicit td: TraceData) = try {
    val conn = source.getConnection

    val st = conn.createStatement()
    val rs = st.executeQuery("select count(*) x from information_schema.tables where table_name = 'flyway_schema_history'")
    if (!rs.next()) {
      throw new IllegalStateException()
    }

    val initialized = rs.getBoolean(1)

    rs.close()
    conn.close()

    val flyway = if (initialized) {
      Flyway.configure().dataSource(source).load()
    } else {
      val f = Flyway.configure().dataSource(source).baselineVersion("0").load()
      log.info("Initializing Flyway database migrations")
      f.baseline()
      f
    }
    val numApplied = flyway.migrate()
    if (numApplied > 0) {
      log.info(s"Applied [$numApplied] new database migrations")
    }
  } catch {
    case NonFatal(x) => log.error("Error running Flyway migrations", x)
  }
}
