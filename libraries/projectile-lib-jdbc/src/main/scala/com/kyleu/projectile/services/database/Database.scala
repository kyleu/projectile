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


/*

Tl;DR: We were using ExecutionContext.global for our database, which blocks on all requests. Switching to a bespoke ExecutionContext solved the issue.

Several weeks ago, CoCo collapsed under load. It happened again yesterday and today. The behavior was the same in all cases: svc-offer showed very long requests to other services, where they would pause for several seconds before performing the work in the expected time.

Dorian worked with me to disagnose this issue for most of yesterday. We initially suspected the ExecutionContext, but only in the "context" of Play's HTTP client (PlayWS). We devised several benchmarks to stress PlayWS in local and production deployments, but didn't find anything. Also, I had computer issues, where my machine froze after screensharing for a length of time. This always happened at the most critical point in our testing, and we gave up to grab dinner.

Dorian and I planned to restart our troubleshooting session this morning, but since the issue happened again, it forced our hand early. We hopped on a call with Mike, Chinmay, Jesse, and Graeme to troubleshoot. We used the excellent tracing facilities in Datadog to compare requests from our current deployment against traces from previous troubles and normal operation. At this point we weren't certain all three issues were the same cause, so we used the excellent deployment facilities in K8S to roll back to several deployments, all without effect. After studying the traces, it was determined that we were clearly waiting on a full queue, as the CPU, disk, memory, and network pressure were all normal.

Once we realized (or, in my case, remembered) that our current database is backed by (blocking) JDBC, we tracked the issue down to lib-fevo/.../Database.scala, which uses the global ExecutionContext provided by Scala. That context has a size equal to the number of cpus on the server. In our case, that meant that only eight queries could execute at a time, and all other clients with references to global waited as well.

We changed Database to use a new ExecutionContext, set to "32" slots, the same amount Hikari, our connection pool uses. We also added logging to detect long checkout times for our database pool. We biffed one deployment, as we had branched off a newer version of lib-fevo (8.3.4) than CoCo could handle. We branched off of `8.3.1`, and this resolved the issue.

This only showed up recently because of database load in svc-offer. The other services would have the same issue, but never reached the scale necessary

I've submitted PR #49 for the change to `lib-fevo`. Once merged, we should update every project as soon as possible. Long term, all references to ExecutionContext.global should be replaced with dependency-injected objects.
 */
