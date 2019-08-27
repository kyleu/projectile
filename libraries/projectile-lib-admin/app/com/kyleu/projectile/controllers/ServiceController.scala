package com.kyleu.projectile.controllers

import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.services.ModelServiceHelper
import com.kyleu.projectile.util.Credentials
import com.kyleu.projectile.util.tracing.TraceData

abstract class ServiceController[T](val svc: ModelServiceHelper[T]) extends BaseController(svc.key) {
  protected def search(q: Option[String], orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int])(implicit traceData: TraceData) = {
    q match {
      case Some(query) if query.nonEmpty => svc.search(Credentials.system, q, Nil, orderBys, limit.orElse(Some(100)), offset)
      case _ => svc.getAll(Credentials.system, Nil, orderBys, limit.orElse(Some(100)), offset)
    }
  }

  protected def searchWithCount(
    q: Option[String], orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int]
  )(implicit traceData: TraceData) = q match {
    case Some(query) if query.nonEmpty => svc.searchWithCount(Credentials.system, q, Nil, orderBys, limit.orElse(Some(100)), offset)
    case _ => svc.getAllWithCount(Credentials.system, Nil, orderBys, limit.orElse(Some(100)), offset)
  }
}
