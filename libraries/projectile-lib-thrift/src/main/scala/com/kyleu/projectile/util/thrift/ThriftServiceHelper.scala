package com.kyleu.projectile.util.thrift

import com.kyleu.projectile.util.tracing

import scala.concurrent.Future

object ThriftServiceHelper {
  trait TraceHelper {
    def trace[A](traceName: String, tags: (String, String)*)(f: tracing.TraceData => Future[A])(implicit parentData: tracing.TraceData): Future[A]
  }

  private var traceHelperOpt: Option[TraceHelper] = None

  def setTraceHelper(traceHelper: TraceHelper) = traceHelperOpt = Some(traceHelper)
}

abstract class ThriftServiceHelper(val name: String) {
  protected def trace[A](traceName: String, tags: (String, String)*)(f: tracing.TraceData => Future[A])(implicit parentData: tracing.TraceData) = {
    ThriftServiceHelper.traceHelperOpt.map { tracing =>
      tracing.trace(s"thrift.$name.$traceName", tags: _*)(f)
    }.getOrElse(f(parentData))
  }

  def healthcheck(implicit td: tracing.TraceData) = {
    Future.successful(name + ": OK")
  }
}
