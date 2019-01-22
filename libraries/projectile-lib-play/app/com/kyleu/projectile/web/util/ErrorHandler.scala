package com.kyleu.projectile.web.util

import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.tracing.{OpenTracingService, TraceData}
import io.circe.Json
import javax.inject._
import play.api._
import play.api.http.{DefaultHttpErrorHandler, MimeTypes}
import play.api.mvc._
import play.api.routing.Router
import play.twirl.api.Html

import scala.concurrent._

object ErrorHandler {
  trait Actions {
    def badRequest(path: String, error: String)(implicit session: Session, flash: Flash, td: TraceData): Html
    def serverError(error: String, ex: Option[Throwable])(implicit session: Session, flash: Flash, td: TraceData): Html
    def notFound(path: String)(implicit session: Session, flash: Flash, td: TraceData): Html
  }
}

@javax.inject.Singleton
class ErrorHandler @Inject() (
    actions: ErrorHandler.Actions, env: Environment, config: Configuration,
    sourceMapper: OptionalSourceMapper, router: Provider[Router], tracing: OpenTracingService
) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) with Rendering with AcceptExtractors with Logging {

  override protected def onDevServerError(request: RequestHeader, ex: UsefulException) = tracing.topLevelTrace("error.dev") { td =>
    td.tag("error.type", ex.getClass.getSimpleName)
    td.tag("error.message", ex.getMessage)
    td.tag("error.stack", ex.getStackTrace.mkString("\n"))
    render.async {
      case Accepts.Json() => jsonError(request, ex)
      case _ => super.onDevServerError(request, ex)
    }(request)
  }

  override def onProdServerError(request: RequestHeader, ex: UsefulException) = tracing.topLevelTrace("error.prod") { td =>
    td.tag("error.type", ex.getClass.getSimpleName)
    td.tag("error.message", ex.getMessage)
    td.tag("error.stack", ex.getStackTrace.mkString("\n"))
    render.async {
      case Accepts.Json() => jsonError(request, ex)
      case _ => Future.successful {
        val result = actions.serverError(request.path, Some(ex))(request.session, request.flash, td)
        Results.InternalServerError(result)
      }
    }(request)
  }

  override def onClientError(request: RequestHeader, statusCode: Int, message: String) = tracing.topLevelTrace("not.found") { td =>
    td.tag("error.type", "client.error")
    td.tag("error.message", message)
    render.async {
      case Accepts.Json() => jsonNotFound(request, statusCode, message)
      case _ => Future.successful {
        val result = actions.notFound(request.path)(request.session, request.flash, td)
        Results.NotFound(result)
      }
    }(request)
  }

  override protected def onBadRequest(request: RequestHeader, error: String) = tracing.topLevelTrace("not.found") { td =>
    td.tag("error.type", "bad.request")
    td.tag("error.message", error)
    val result = actions.badRequest(request.path, error)(request.session, request.flash, td)
    Future.successful(Results.BadRequest(result))
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
}
