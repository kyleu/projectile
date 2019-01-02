package util.web

import javax.inject.Inject
import akka.stream.Materializer
import com.kyleu.projectile.util.tracing.TraceData
import play.api.mvc._
import com.kyleu.projectile.util.{Config, Logging}

import scala.concurrent.{ExecutionContext, Future}

class LoggingFilter @Inject() (override implicit val mat: Materializer) extends Filter with Logging {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val metricsName = Config.metricsId + "_http_requests"

  def apply(nextFilter: RequestHeader => Future[Result])(request: RequestHeader): Future[Result] = {
    val startNanos = System.nanoTime

    nextFilter(request).transform(
      result => {
        if (request.path.startsWith("/assets")) {
          result
        } else {
          val requestTime = System.nanoTime - startNanos
          log.info(s"${result.header.status} (${requestTime / 1000000000.0}s): ${request.method} ${request.uri}")(TraceData.noop)
          result.withHeaders("X-Request-Time-Ms" -> (requestTime * 1000000).toInt.toString)
        }
      },
      exception => {
        exception
      }
    )
  }
}
