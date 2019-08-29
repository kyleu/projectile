package com.kyleu.projectile.util.tracing

import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.metrics.MetricsConfig
import io.jaegertracing.Configuration
import io.jaegertracing.micrometer.MicrometerMetricsFactory
import io.opentracing.Tracer
import io.opentracing.noop.NoopTracerFactory
import io.opentracing.propagation.{Format, TextMapInject, TextMapInjectAdapter}
import scala.jdk.CollectionConverters._

class OpenTracingTracer(cnf: MetricsConfig) extends Logging {
  private[this] var cfg: Option[Configuration] = None

  val tracer: Tracer = if (cnf.tracingEnabled) {
    val sampler = new Configuration.SamplerConfiguration().withType("const").withParam(1)
    val sender = new Configuration.SenderConfiguration().withAgentHost(cnf.tracingServer).withAgentPort(cnf.tracingPort)
    val reporter = new Configuration.ReporterConfiguration().withLogSpans(false).withSender(sender).withFlushInterval(1000).withMaxQueueSize(10000)
    val metrics = new MicrometerMetricsFactory()

    val loc = s"${cnf.tracingServer}:${cnf.tracingPort}"
    log.info(s"Tracing enabled, sending results to [$loc] using sample rate [${cnf.tracingSampleRate}]")(TraceData.noop)
    val c = new Configuration("project").withSampler(sampler).withReporter(reporter).withMetricsFactory(metrics)
    cfg = Some(c)
    c.getTracer
  } else {
    NoopTracerFactory.create()
  }

  def toMap(td: TraceData) = td match {
    case tdz: TraceDataOpenTracing =>
      val data = new java.util.HashMap[String, String]()
      tracer.inject(tdz.span.context, Format.Builtin.HTTP_HEADERS.asInstanceOf[Format[TextMapInject]], new TextMapInjectAdapter(data))
      data.asScala
    case _ => Map.empty
  }

  def close() = {
    cfg.foreach(_.closeTracer())
    cfg = None
  }
}
