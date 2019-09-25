package com.kyleu.projectile.controllers

import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.metrics.Instrumented
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}
import com.kyleu.projectile.models.web.{ControllerUtils, TracingFilter}
import io.circe.{Json, Printer}
import play.api.http.{ContentTypeOf, MimeTypes, Writeable}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

object BaseController {
  object MimeTypes {
    val csv = "text/csv"
    val png = "image/png"
    val svg = "image/svg+xml"
  }

  val acceptsCsv = Accepting(MimeTypes.csv)
  val acceptsPng = Accepting(MimeTypes.png)
  val acceptsSvg = Accepting(MimeTypes.svg)
}

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

  protected def getTraceData(implicit requestHeader: RequestHeader) = requestHeader.attrs.get(TracingFilter.traceKey).getOrElse(TraceData.noop)

  protected implicit val contentTypeOfJson: ContentTypeOf[Json] = ContentTypeOf(Some("application/json"))
  protected implicit def writableOfJson(implicit codec: Codec, printer: Printer = Printer.spaces2): Writeable[Json] = {
    Writeable(a => codec.encode(a.printWith(printer)))
  }

  protected def modelForm(body: AnyContent) = body.asFormUrlEncoded match {
    case Some(x) => ControllerUtils.modelForm(x)
    case None => ControllerUtils.jsonBody(body).as[Seq[DataField]].getOrElse(throw new IllegalStateException("Json must be an array of DataFields"))
  }

  protected def enhanceRequest(request: Request[AnyContent], trace: TraceData) = {
    trace.tag("http.request.size", request.body.asText.map(_.length).orElse(request.body.asRaw.map(_.size.toInt)).getOrElse(0).toString)
  }

  protected def renderChoice(t: Option[String])(f: String => Result)(implicit request: RequestHeader) = t match {
    case None => render {
      case Accepts.Html() => f(MimeTypes.HTML)
      case Accepts.Json() => f(MimeTypes.JSON)
      case BaseController.acceptsCsv() => f(BaseController.MimeTypes.csv)
      case BaseController.acceptsPng() => f(BaseController.MimeTypes.png)
      case BaseController.acceptsSvg() => f(BaseController.MimeTypes.svg)
    }
    case Some("csv") => f(BaseController.MimeTypes.csv)
    case Some("html") => f(MimeTypes.HTML)
    case Some("json") => f(MimeTypes.JSON)
    case Some("png") => f(BaseController.MimeTypes.png)
    case Some("svg") => f(BaseController.MimeTypes.svg)
    case Some(x) => throw new IllegalStateException(s"Unhandled output format [$x]")
  }

  protected def csvResponse(filename: String, content: String) = {
    val fn = if (filename.endsWith(".csv")) { filename } else { filename + ".csv" }
    Ok(content).as(BaseController.MimeTypes.csv).withHeaders("Content-Disposition" -> s"""inline; filename="$fn"""")
  }
}
