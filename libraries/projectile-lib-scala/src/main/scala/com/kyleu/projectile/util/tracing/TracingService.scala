package com.kyleu.projectile.util.tracing

import com.kyleu.projectile.util.Logging

import scala.concurrent.Future

object TracingService {
  val noop = new TracingService {
    override def topLevelTrace[A](name: String)(f: TraceData => Future[A]) = f(TraceData.noop)
    override def topLevelTraceBlocking[A](name: String)(f: TraceData => A) = f(TraceData.noop)
    override def trace[A](traceName: String, tags: (String, String)*)(f: TraceData => Future[A])(implicit parentData: TraceData) = f(TraceData.noop)
    override def traceBlocking[A](traceName: String, tags: (String, String)*)(f: TraceData => A)(implicit parentData: TraceData) = f(TraceData.noop)
    override def close() = {}
  }
}

trait TracingService extends Logging {
  def noopTrace[A](name: String)(f: TraceData => Future[A]): Future[A] = f(TraceData.noop)
  def topLevelTrace[A](name: String)(f: TraceData => Future[A]): Future[A]
  def topLevelTraceBlocking[A](name: String)(f: TraceData => A): A

  def trace[A](traceName: String, tags: (String, String)*)(f: TraceData => Future[A])(implicit parentData: TraceData): Future[A]
  def traceBlocking[A](traceName: String, tags: (String, String)*)(f: TraceData => A)(implicit parentData: TraceData): A

  def close(): Unit
}
