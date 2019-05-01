package com.kyleu.projectile.web.util

import akka.stream.Materializer
import com.kyleu.projectile.util.tracing.{OpenTracingService, TraceData, TraceDataOpenTracing}
import javax.inject.Inject
import play.api.libs.typedmap.TypedKey
import play.api.mvc.{Filter, RequestHeader, Result}
import play.api.routing.Router

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object TracingFilter {
  val traceKey = TypedKey[TraceData]("trace")

  val paramAwareRequestNamer: RequestHeader => String = { reqHeader =>
    import org.apache.commons.lang3.StringUtils
    val pathPattern = StringUtils.replace(reqHeader.attrs.get(Router.Attrs.HandlerDef).map(_.path).getOrElse(reqHeader.path), "<[^/]+>", "")
    s"${reqHeader.method} - $pathPattern"
  }
}

class TracingFilter @Inject() (tracingService: OpenTracingService)(implicit val mat: Materializer, ec: ExecutionContext) extends Filter {
  private val reqHeaderToSpanName: RequestHeader => String = TracingFilter.paramAwareRequestNamer

  def apply(nextFilter: RequestHeader => Future[Result])(req: RequestHeader): Future[Result] = if (LoggingFilter.skipPath(req.path)) {
    nextFilter(req)
  } else {
    val serverSpan = tracingService.serverReceived(
      spanName = reqHeaderToSpanName(req),
      span = tracingService.newSpan("webrequest", req.headers.toSimpleMap).start()
    )
    serverSpan.setTag("http.host", req.host)
    serverSpan.setTag("http.method", req.method)
    serverSpan.setTag("http.path", req.path)
    if (req.queryString.nonEmpty) {
      serverSpan.setTag("http.query.string", req.rawQueryString)
    }
    req.queryString.foreach {
      case (k, v) => serverSpan.setTag(s"http.query.$k", v.mkString(", "))
    }

    val result = nextFilter(req.addAttr(TracingFilter.traceKey, TraceDataOpenTracing(serverSpan)))
    result.onComplete {
      case Failure(t) => tracingService.serverSend(serverSpan, "failed" -> s"Finished with exception: ${t.getMessage}")
      case Success(x) =>
        serverSpan.setTag("http.status.code", x.header.status.toString)
        x.header.headers.get("Content-Type").map(c => serverSpan.setTag("http.response.contentType", c))
        x.body.contentLength.map(l => serverSpan.setTag("http.response.size", l.toString))
        tracingService.serverSend(serverSpan)
    }
    result
  }
}
