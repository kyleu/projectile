package com.kyleu.projectile.controllers

import com.kyleu.projectile.models.result.orderBy.OrderBy
import play.api.http.MimeTypes
import play.api.mvc._
import com.kyleu.projectile.services.{Credentials, ModelServiceHelper}
import com.kyleu.projectile.util.tracing.TraceData

object ServiceController {
  object MimeTypes {
    val csv = "text/csv"
    val png = "image/png"
    val svg = "image/svg+xml"
  }

  val acceptsCsv = Accepting(MimeTypes.csv)
  val acceptsPng = Accepting(MimeTypes.png)
  val acceptsSvg = Accepting(MimeTypes.svg)
}

abstract class ServiceController[T](val svc: ModelServiceHelper[T]) extends BaseController(svc.key) {
  protected def search(q: Option[String], orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int])(implicit traceData: TraceData) = {
    q match {
      case Some(query) if query.nonEmpty => svc.search(Credentials.system, q, Nil, orderBys, limit.orElse(Some(100)), offset)
      case _ => svc.getAll(Credentials.system, Nil, orderBys, limit.orElse(Some(100)), offset)
    }
  }

  protected def searchWithCount(
    q: Option[String], orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int]
  )(implicit traceData: TraceData) = q match {
    case Some(query) if query.nonEmpty => svc.searchWithCount(Credentials.system, q, Nil, orderBys, limit.orElse(Some(100)), offset)
    case _ => svc.getAllWithCount(Credentials.system, Nil, orderBys, limit.orElse(Some(100)), offset)
  }

  protected def renderChoice(t: Option[String])(f: String => Result)(implicit request: RequestHeader) = t match {
    case None => render {
      case Accepts.Html() => f(MimeTypes.HTML)
      case Accepts.Json() => f(MimeTypes.JSON)
      case ServiceController.acceptsCsv() => f(ServiceController.MimeTypes.csv)
      case ServiceController.acceptsPng() => f(ServiceController.MimeTypes.png)
      case ServiceController.acceptsSvg() => f(ServiceController.MimeTypes.svg)
    }
    case Some("csv") => f(ServiceController.MimeTypes.csv)
    case Some("html") => f(MimeTypes.HTML)
    case Some("json") => f(MimeTypes.JSON)
    case Some("png") => f(ServiceController.MimeTypes.png)
    case Some("svg") => f(ServiceController.MimeTypes.svg)
    case Some(x) => throw new IllegalStateException(s"Unhandled output format [$x].")
  }

  def csvResponse(filename: String, content: String) = {
    val fn = if (filename.endsWith(".csv")) { filename } else { filename + ".csv" }
    Ok(content).as(ServiceController.MimeTypes.csv).withHeaders("Content-Disposition" -> s"""inline; filename="$fn.csv"""")
  }
}
