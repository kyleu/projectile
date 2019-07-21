package com.kyleu.projectile.services.database

import com.kyleu.projectile.models.database.{DatabaseConfig, RawQuery, Statement}
import com.kyleu.projectile.models.queries.CommonQueries
import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}

import scala.concurrent.{ExecutionContext, Future}

trait Database[Conn] extends Logging {
  protected[this] implicit def ec: ExecutionContext
  protected[this] def key: String

  def transaction[A](f: (TraceData, Conn) => A)(implicit traceData: TraceData): A

  def execute(statement: Statement, conn: Option[Conn] = None)(implicit traceData: TraceData): Int
  def executeF(statement: Statement, conn: Option[Conn] = None)(implicit traceData: TraceData): Future[Int] = Future(execute(statement, conn))
  def query[A](q: RawQuery[A], conn: Option[Conn] = None)(implicit traceData: TraceData): A
  def queryF[A](q: RawQuery[A], conn: Option[Conn] = None)(implicit traceData: TraceData): Future[A] = Future(query(q, conn))

  def close() = {
    tracingServiceOpt = None
    config = None
    started = false
  }

  private[this] var tracingServiceOpt: Option[TracingService] = None
  protected def tracing = tracingServiceOpt.getOrElse(TracingService.noop)

  private[this] var config: Option[DatabaseConfig] = None
  def getConfig = config.getOrElse(throw new IllegalStateException("Database not open"))

  private[this] var started: Boolean = false
  def isStarted = started
  protected[this] def start(cfg: DatabaseConfig, svc: TracingService) = {
    tracingServiceOpt = Some(svc)
    config = Some(cfg)
    started = true
  }

  def doesTableExist(name: String)(implicit td: TraceData): Boolean = (!isStarted) || query(CommonQueries.DoesTableExist(name))

  protected[this] def prependComment(obj: Object, sql: String) = s"/* ${obj.getClass.getSimpleName.replace("$", "")} */ $sql"

  protected[this] def trace[A](traceName: String)(f: TraceData => A)(implicit traceData: TraceData) = tracing.traceBlocking(key + "." + traceName) { td =>
    f(td)
  }
}
