package com.kyleu.projectile.web.util

import java.util.concurrent.TimeUnit

import akka.stream.Materializer
import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.metrics.Instrumented
import com.kyleu.projectile.util.tracing.TraceData
import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object LoggingFilter {
  def skipPath(p: String) = p.startsWith("/assets") || p.startsWith("/style") || p.startsWith("/components")
}

class LoggingFilter @Inject() (override implicit val mat: Materializer) extends Filter with Logging {
  val metricsName = "http_requests"

  def apply(nextFilter: RequestHeader => Future[Result])(request: RequestHeader): Future[Result] = {
    val startNanos = System.nanoTime

    def logCompleted(result: Result): Unit = {
      Instrumented.regOpt.foreach(_.counter(metricsName + "_count", "result", result.getClass.getSimpleName).increment())
    }

    nextFilter(request).transform(
      result => {
        if (LoggingFilter.skipPath(request.path)) {
          result
        } else {
          logCompleted(result)
          val requestTime = System.nanoTime - startNanos
          Instrumented.regOpt.foreach(_.timer(metricsName, "result", result.getClass.getSimpleName).record(requestTime, TimeUnit.NANOSECONDS))
          log.info(s"${result.header.status} (${requestTime / 1000000000.0}s): ${request.method} ${request.uri}")(TraceData.noop)
          result.withHeaders("X-Request-Time-Ms" -> (requestTime * 1000000).toInt.toString)
        }
      },
      exception => {
        logCompleted(Results.InternalServerError)
        exception
      }
    )
  }
}
