package com.kyleu.projectile.controllers

import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.metrics.Instrumented
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}
import com.kyleu.projectile.web.util.{ControllerUtils, TracingFilter}
import io.circe.{Json, Printer}
import play.api.http.{ContentTypeOf, Writeable}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

abstract class BaseController(val name: String) extends InjectedController with Logging {
  def tracing: TracingService

  private[this] def cn(x: Any) = x.getClass.getSimpleName.replaceAllLiterally("$", "")

  protected[this] lazy val metricsName = cn(this)

  protected def act(action: String)(block: Request[AnyContent] => TraceData => Future[Result])(implicit ec: ExecutionContext) = {
    Action.async { implicit request =>
      Instrumented.timeFuture(metricsName + "_request", "action", name + "_" + action) {
        tracing.trace(name + ".controller." + action) { td =>
          enhanceRequest(request, td)
          block(request)(td)
        }(getTraceData)
      }
    }
  }

  protected def getTraceData(implicit requestHeader: RequestHeader) = requestHeader.attrs(TracingFilter.traceKey)

  private[this] val defaultPrinter = Printer.spaces2
  protected implicit val contentTypeOfJson: ContentTypeOf[Json] = ContentTypeOf(Some("application/json"))
  protected implicit def writableOfJson(implicit codec: Codec, printer: Printer = defaultPrinter): Writeable[Json] = {
    Writeable(a => codec.encode(a.pretty(printer)))
  }

  protected def modelForm(body: AnyContent) = body.asFormUrlEncoded match {
    case Some(x) => ControllerUtils.modelForm(x)
    case None => ControllerUtils.jsonBody(body).as[Seq[DataField]].getOrElse(throw new IllegalStateException("Json must be an array of DataFields."))
  }

  protected def enhanceRequest(request: Request[AnyContent], trace: TraceData) = {
    trace.tag("http.request.size", request.body.asText.map(_.length).orElse(request.body.asRaw.map(_.size.toInt)).getOrElse(0).toString)
  }
}
