package com.kyleu.projectile.models.web

import com.kyleu.projectile.services.error.ErrorLoggingService
import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}
import io.circe.Json
import play.api._
import play.api.http.{DefaultHttpErrorHandler, MimeTypes}
import play.api.mvc._
import play.api.routing.Router
import play.twirl.api.Html

import scala.concurrent._

object ErrorHandler {
  class Actions() {
    def badRequest(path: String, error: String)(implicit flash: Flash): Html = {
      com.kyleu.projectile.views.html.error.badRequest(path, error)
    }
    def serverError(error: String, ex: Option[Throwable])(implicit flash: Flash): Html = {
      com.kyleu.projectile.views.html.error.serverError(error, ex)
    }
    def notFound(path: String)(implicit flash: Flash): Html = {
      com.kyleu.projectile.views.html.error.notFound(path)
    }
  }
}

@javax.inject.Singleton
class ErrorHandler @javax.inject.Inject() (
    actions: ErrorHandler.Actions, env: Environment, config: Configuration, errorLoggingService: ErrorLoggingService,
    sourceMapper: OptionalSourceMapper, router: javax.inject.Provider[Router], tracing: TracingService
) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) with Rendering with AcceptExtractors with Logging {

  override protected def onDevServerError(request: RequestHeader, ex: UsefulException) = tracing.topLevelTrace("error.dev") { td =>
    td.tag("error.type", ex.getClass.getSimpleName)
    td.tag("error.message", ex.getMessage)
    td.tag("error.stack", ex.getStackTrace.mkString("\n"))
    errorLoggingService.record(None, request.uri.take(2048), ex)(TraceData.noop)
    render.async {
      case Accepts.Json() => jsonError(request, ex)
      case _ => Future.successful(Results.InternalServerError(actions.serverError(request.path, Some(ex))(request.flash)))
      // case _ => super.onDevServerError(request, ex)
    }(request)
  }

  override def onProdServerError(request: RequestHeader, ex: UsefulException) = tracing.topLevelTrace("error.prod") { td =>
    td.tag("error.type", ex.getClass.getSimpleName)
    td.tag("error.message", ex.getMessage)
    td.tag("error.stack", ex.getStackTrace.mkString("\n"))
    errorLoggingService.record(None, request.uri.take(2048), ex)(TraceData.noop)
    render.async {
      case Accepts.Json() => jsonError(request, ex)
      case _ => Future.successful(Results.InternalServerError(actions.serverError(request.path, Some(ex))(request.flash)))
    }(request)
  }

  override def onClientError(request: RequestHeader, statusCode: Int, message: String) = tracing.topLevelTrace("not.found") { td =>
    td.tag("error.type", "client.error")
    td.tag("error.message", message)
    render.async {
      case Accepts.Json() => jsonNotFound(request, statusCode, message)
      case _ => Future.successful(Results.NotFound(actions.notFound(request.path)(request.flash)))
    }(request)
  }

  override protected def onBadRequest(request: RequestHeader, error: String) = tracing.topLevelTrace("not.found") { td =>
    td.tag("error.type", "bad.request")
    td.tag("error.message", error)
    render.async {
      case Accepts.Json() => jsonBadRequest(request, error)
      case _ => Future.successful(Results.BadRequest(actions.badRequest(request.path, error)(request.flash)))
    }(request)
  }

  private[this] def jsonError(request: RequestHeader, ex: UsefulException) = Future.successful(Results.InternalServerError(Json.obj(
    "status" -> Json.fromString("error"),
    "t" -> Json.fromString(ex.getClass.getSimpleName),
    "message" -> Json.fromString(ex.getMessage),
    "location" -> Json.fromString(ex.getStackTrace.headOption.map(_.toString).getOrElse("n/a"))
  ).spaces2).as(MimeTypes.JSON))

  private[this] def jsonNotFound(request: RequestHeader, statusCode: Int, message: String) = Future.successful(Results.NotFound(Json.obj(
    "status" -> Json.fromInt(statusCode),
    "message" -> Json.fromString(message)
  ).spaces2).as(MimeTypes.JSON))

  private[this] def jsonBadRequest(request: RequestHeader, error: String) = Future.successful(Results.BadRequest(Json.obj(
    "error" -> Json.fromString(error)
  ).spaces2).as(MimeTypes.JSON))
}
