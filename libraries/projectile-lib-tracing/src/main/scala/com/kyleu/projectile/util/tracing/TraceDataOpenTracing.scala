package com.kyleu.projectile.util.tracing

import datadog.opentracing.DDSpanContext
import io.jaegertracing.internal.JaegerSpanContext
import io.opentracing.Span

final case class TraceDataOpenTracing(span: Span) extends TraceData {
  override val isNoop = false

  override def tag(k: String, v: String) = span.setTag(k, v)
  override def annotate(v: String) = span.log(v)

  override def logClass(cls: Class[_]): Unit = span.log(cls.getSimpleName.stripSuffix("$"))

  override val (traceId, spanId) = span.context match {
    case j: JaegerSpanContext => j.getTraceId.toString -> j.getSpanId.toString
    case d: DDSpanContext => d.getTraceId -> d.getSpanId
  }

  override def toString = span.context.toString
}
