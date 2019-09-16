package com.kyleu.projectile.services

import java.sql.Connection

import com.kyleu.projectile.models.result.filter.Filter
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.util.Credentials
import com.kyleu.projectile.util.tracing.TraceData

import scala.concurrent.Future

trait ModelService[T] {
  def countAll(creds: Credentials, filters: Seq[Filter], conn: Option[Connection] = None)(implicit trace: TraceData): Future[Int]
  def getAll(
    creds: Credentials, filters: Seq[Filter], orderBys: Seq[OrderBy], limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData): Future[Seq[T]]

  def getAllWithCount(
    creds: Credentials, filters: Seq[Filter], orderBys: Seq[OrderBy], limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData): Future[(Int, Seq[T])]

  def searchCount(creds: Credentials, q: Option[String], filters: Seq[Filter], conn: Option[Connection] = None)(implicit trace: TraceData): Future[Int]
  def search(
    creds: Credentials, q: Option[String], filters: Seq[Filter], orderBys: Seq[OrderBy],
    limit: Option[Int], offset: Option[Int], conn: Option[Connection] = None
  )(implicit trace: TraceData): Future[Seq[T]]

  def searchWithCount(
    creds: Credentials, q: Option[String], filters: Seq[Filter], orderBys: Seq[OrderBy],
    limit: Option[Int] = None, offset: Option[Int] = None, conn: Option[Connection] = None
  )(implicit trace: TraceData): Future[(Int, Seq[T])]
}
