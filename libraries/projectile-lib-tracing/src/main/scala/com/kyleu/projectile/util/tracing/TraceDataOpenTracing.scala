package com.kyleu.projectile.util.tracing

import datadog.opentracing.DDSpanContext
import io.jaegertracing.internal.JaegerSpanContext
import io.opentracing.Span
import io.opentracing.noop.NoopSpanContext

/** Extends TraceData with OpenTracing-specific functionality */
final case class TraceDataOpenTracing(span: Span) extends TraceData {
  override val isNoop = false

  override def tag(k: String, v: String) = span.setTag(k, v)
  override def annotate(v: String) = span.log(v)

  override def logClass(cls: Class[_]): Unit = span.log(cls.getSimpleName.stripSuffix("$"))

  override val (traceId, spanId) = span.context match {
    case j: JaegerSpanContext => j.getTraceId -> j.getSpanId.toString
    case d: DDSpanContext => d.getTraceId.toString -> d.getSpanId.toString
    case _: NoopSpanContext => "noop" -> "noop"
    case x => x.getClass.getSimpleName -> "none"
  }

  override def toString = span.context.toString
}
