package com.kyleu.projectile.services

import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.models.result.filter.Filter
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}
import com.kyleu.projectile.util.{Credentials, Logging, NullUtils}

import scala.concurrent.{ExecutionContext, Future}

abstract class ModelServiceHelper[T](val key: String, val perm: (String, String))(implicit ec: ExecutionContext) extends ModelService[T] with Logging {
  def tracing: TracingService

  def traceF[A](name: String)(f: TraceData => Future[A])(implicit trace: TraceData) = tracing.trace(key + ".service." + name)(td => f(td))
  def traceB[A](name: String)(f: TraceData => A)(implicit trace: TraceData) = tracing.traceBlocking(key + ".service." + name)(td => f(td))

  override def countAll(creds: Credentials, filters: Seq[Filter])(implicit trace: TraceData): Future[Int]
  override def getAll(
    creds: Credentials, filters: Seq[Filter], orderBys: Seq[OrderBy], limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData): Future[Seq[T]]

  override def getAllWithCount(
    creds: Credentials, filters: Seq[Filter], orderBys: Seq[OrderBy], limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData): Future[(Int, Seq[T])] = traceF("get.all.with.count") { td =>
    val result = getAll(creds, filters, orderBys, limit, offset)(td)
    val count = countAll(creds, filters)(td)
    count.flatMap(c => result.map(x => c -> x))
  }

  override def searchCount(creds: Credentials, q: Option[String], filters: Seq[Filter])(implicit trace: TraceData): Future[Int]
  override def search(
    creds: Credentials, q: Option[String], filters: Seq[Filter], orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int]
  )(implicit trace: TraceData): Future[Seq[T]]

  override def searchWithCount(
    creds: Credentials, q: Option[String], filters: Seq[Filter], orderBys: Seq[OrderBy], limit: Option[Int] = None, offset: Option[Int] = None
  )(implicit trace: TraceData): Future[(Int, Seq[T])] = traceF("search.with.count") { td =>
    val result = search(creds, q, filters, orderBys, limit, offset)(td)
    val count = searchCount(creds, q, filters)(td)
    count.flatMap(c => result.map(x => c -> x))
  }

  protected def fieldVal(fields: Seq[DataField], k: String) = fields.find(_.k == k).flatMap(_.v).getOrElse(NullUtils.str)

  def checkPerm[Ret](creds: Credentials, key: String)(f: => Ret): Ret = PermissionService.check(creds.role, perm._1, perm._2, key) match {
    case (false, msg) => throw new IllegalStateException(s"Insufficent permissions to access [${perm._1}, ${perm._2}, $key]: $msg")
    case (true, _) => f
  }
}
