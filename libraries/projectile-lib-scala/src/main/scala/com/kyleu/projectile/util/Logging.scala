package com.kyleu.projectile.util

import com.kyleu.projectile.util.tracing.TraceData
import org.slf4j.{LoggerFactory, MDC}

object Logging {
  val metricsId = "logging"

  final case class TraceLogger(cls: Class[_]) {
    private[this] val logger = LoggerFactory.getLogger(cls: Class[_])

    def logCtx[T](f: => T)(implicit td: TraceData) = {
      MDC.put("ddTraceID", td.traceId)
      MDC.put("ddSpanID", td.spanId)
      val ret = f
      MDC.remove("ddTraceID")
      MDC.remove("ddSpanID")
      ret
    }

    def trace(message: => String)(implicit td: TraceData) = logCtx(logger.trace(message))
    def trace(message: => String, e: => Throwable)(implicit td: TraceData) = logCtx(logger.trace(message, e))

    def debug(message: => String)(implicit td: TraceData) = logCtx(logger.debug(message))
    def debug(message: => String, e: => Throwable)(implicit td: TraceData) = logCtx(logger.debug(message, e))

    def info(message: => String)(implicit td: TraceData) = logCtx(logger.info(message))
    def info(message: => String, e: => Throwable)(implicit td: TraceData) = logCtx(logger.info(message, e))

    def warn(message: => String)(implicit td: TraceData) = logCtx(logger.warn(message))
    def warn(message: => String, e: => Throwable)(implicit td: TraceData) = logCtx(logger.warn(message, e))

    def error(message: => String)(implicit td: TraceData) = logCtx(logger.error(message))
    def error(message: => String, e: => Throwable)(implicit td: TraceData) = logCtx(logger.error(message, e))

    def errorThenThrow(message: => String)(implicit td: TraceData) = {
      error(message)
      throw new IllegalStateException(message)
    }
    def errorThenThrow(message: => String, e: => Throwable)(implicit td: TraceData) = {
      error(message, e)
      throw e
    }
  }
}

trait Logging {
  protected[this] lazy val log = Logging.TraceLogger(this.getClass)
}
