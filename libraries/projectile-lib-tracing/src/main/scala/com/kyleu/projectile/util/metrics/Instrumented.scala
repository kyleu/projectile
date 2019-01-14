package com.kyleu.projectile.util.metrics

import java.util.concurrent.TimeUnit

import com.kyleu.projectile.util.tracing.TraceData
import com.kyleu.projectile.util.{Logging, NullUtils}
import io.micrometer.core.instrument.binder.jvm.{ClassLoaderMetrics, JvmGcMetrics, JvmMemoryMetrics, JvmThreadMetrics}
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.{Clock, MeterRegistry, Metrics}
import io.micrometer.prometheus.{PrometheusConfig, PrometheusMeterRegistry}
import io.micrometer.statsd.{StatsdConfig, StatsdFlavor, StatsdMeterRegistry}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

/// Helper class to initialize Prometheus, Datadog, or StatsD metrics reporting
object Instrumented extends Logging {
  private[this] def cn(x: Any) = x.getClass.getSimpleName.replaceAllLiterally("$", "")

  private[this] var registry: Option[MeterRegistry] = None

  def reg = registry.getOrElse(throw new IllegalStateException("Not started"))
  def regOpt = registry

  def start(engine: String, serviceName: String, hostAddress: String) = {
    val r = engine match {
      case "datadog" | "statsd" =>
        val config = new StatsdConfig() {
          override def get(k: String): String = k match {
            case "statsd.host" => hostAddress
            case _ => NullUtils.inst
          }
          override def host() = hostAddress
          override def flavor = StatsdFlavor.DATADOG
        }

        val registry = new StatsdMeterRegistry(config, Clock.SYSTEM)
        registry.config.commonTags("service", s"coco-$serviceName")
        log.info(s"Datadog metrics started using host [${hostAddress}]")(TraceData.noop)
        registry

      case "prometheus" =>
        log.info(s"Prometheus metrics started")(TraceData.noop)
        new PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

      case _ => throw new IllegalStateException(s"Invalid metrics engine [$engine]")
    }
    registry = Some(r)

    new ClassLoaderMetrics().bindTo(r)
    new JvmMemoryMetrics().bindTo(r)
    new JvmGcMetrics().bindTo(r)
    new ProcessorMetrics().bindTo(r)
    new JvmThreadMetrics().bindTo(r)
    Metrics.addRegistry(r)
  }

  def stop() = {
    registry.foreach {
      case p: PrometheusMeterRegistry => p.getPrometheusRegistry.clear()
      case _: StatsdMeterRegistry => // Noop
    }
    registry.foreach(_.close())
    registry = None
  }

  def timeReceive[A](msg: Any, key: String, tags: String*)(f: => A) = registry.map { r =>
    val startNanos = System.nanoTime
    try {
      val ret = f
      r.timer(key, ("class" :: cn(msg) :: Nil) ++ tags: _*).record(System.nanoTime - startNanos, TimeUnit.NANOSECONDS)
      ret
    } catch {
      case NonFatal(x) =>
        r.timer(key, ("class" :: cn(msg) :: "error" :: cn(x) :: Nil) ++ tags: _*).record(System.nanoTime - startNanos, TimeUnit.NANOSECONDS)
        throw x
    }
  }.getOrElse(f)

  def timeFuture[A](key: String, tags: String*)(future: => Future[A])(implicit context: ExecutionContext): Future[A] = registry.map { r =>
    val startNanos = System.nanoTime
    val f = try {
      future
    } catch {
      case NonFatal(ex) =>
        throw ex
    }
    f.onComplete {
      case Success(s) => r.timer(key, ("class" :: cn(s) :: Nil) ++ tags: _*).record(System.nanoTime - startNanos, TimeUnit.NANOSECONDS)
      case Failure(x) => r.timer(key, ("error" :: cn(x) :: Nil) ++ tags: _*).record(System.nanoTime - startNanos, TimeUnit.NANOSECONDS)
    }
    f
  }.getOrElse(future)
}
