package com.kyleu.projectile.controllers

import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.services.ModelServiceHelper
import com.kyleu.projectile.util.tracing.TraceData

abstract class ServiceAuthController[T](val svc: ModelServiceHelper[T]) extends AuthController(svc.key) {
  protected def search(q: Option[String], orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int])(implicit request: Req, traceData: TraceData) = {
    q match {
      case Some(query) if query.nonEmpty => svc.search(request, q, Nil, orderBys, limit.orElse(Some(100)), offset)
      case _ => svc.getAll(request, Nil, orderBys, limit.orElse(Some(100)), offset)
    }
  }

  protected def searchWithCount(
    q: Option[String], orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int]
  )(implicit request: Req, traceData: TraceData) = {
    q match {
      case Some(query) if query.nonEmpty => svc.searchWithCount(request, q, Nil, orderBys, limit.orElse(Some(100)), offset)
      case _ => svc.getAllWithCount(request, Nil, orderBys, limit.orElse(Some(100)), offset)
    }
  }
}
