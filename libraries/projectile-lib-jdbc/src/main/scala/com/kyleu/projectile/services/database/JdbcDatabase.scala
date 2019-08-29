package com.kyleu.projectile.services.database

import java.sql.Connection
import java.util.Properties

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import com.kyleu.projectile.models.database.jdbc.Queryable
import com.kyleu.projectile.models.database.{DatabaseConfig, RawQuery, Statement}
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class JdbcDatabase(override val key: String, configPrefix: String)(implicit val ec: ExecutionContext) extends Database[Connection] with Queryable {
  protected val metricsId = s"${key}_database"
  private[this] def time[A](method: String, name: String)(f: => A) = f
  private[this] var ds: Option[HikariDataSource] = None
  def source = ds.getOrElse(throw new IllegalStateException("Database not initialized"))

  def open(cfg: com.typesafe.config.Config, tracing: TracingService) = {
    ds.foreach(_ => close())

    Class.forName("org.postgresql.Driver")
    val config = DatabaseConfig.fromConfig(cfg, configPrefix)
    val properties = new Properties
    val maxPoolSize = 32

    val poolConfig = new HikariConfig(properties) {
      setPoolName(key)
      setJdbcUrl(config.url)
      setUsername(config.username)
      setPassword(config.password.getOrElse(""))
      setConnectionTimeout(10000)
      setMinimumIdle(1)
      setMaximumPoolSize(maxPoolSize)
    }

    val poolDataSource = new HikariDataSource(poolConfig)
    ds = Some(poolDataSource)
    start(config, tracing)
  }

  override def transaction[A](f: (TraceData, Connection) => A)(implicit traceData: TraceData) = trace("transaction") { td =>
    val connection = source.getConnection
    connection.setAutoCommit(false)
    try {
      val result = f(td, connection)
      connection.commit()
      result
    } catch {
      case NonFatal(x) =>
        connection.rollback()
        throw x
    } finally {
      connection.close()
    }
  }

  override def execute(statement: Statement, conn: Option[Connection])(implicit traceData: TraceData) = trace("execute." + statement.name) { td =>
    td.tag("SQL", statement.sql)
    val connection = conn.getOrElse(source.getConnection)
    try {
      time("execute", statement.getClass.getName) { executeUpdate(connection, statement) }
    } catch {
      case NonFatal(x) =>
        val v = statement.values.mkString(", ")
        log.error(s"Error executing [${statement.name}] with [${statement.values.size}] values and sql [${statement.sql}] (Values: $v)", x)
        throw x
    } finally {
      if (conn.isEmpty) { connection.close() }
    }
  }

  override def query[A](query: RawQuery[A], conn: Option[Connection])(implicit traceData: TraceData) = trace("query." + query.name) { td =>
    td.tag("SQL", query.sql)
    val connection = conn.getOrElse(source.getConnection)
    try {
      time[A]("query", query.getClass.getName)(apply(connection, query))
    } catch {
      case NonFatal(x) =>
        val v = query.values.mkString(", ")
        log.error(s"Error running query [${query.name}] with [${query.values.size}] values and sql [${query.sql}] (Values: $v)", x)
        throw x
    } finally {
      if (conn.isEmpty) { connection.close() }
    }
  }

  def withConnection[T](f: Connection => T) = {
    val conn = source.getConnection()
    try { f(conn) } finally { conn.close() }
  }

  override def close() = {
    ds.foreach(_.close())
    ds = None
    super.close()
  }
}
