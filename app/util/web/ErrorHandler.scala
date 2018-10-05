package util.web

import io.circe.Json
import javax.inject._
import play.api.http.{DefaultHttpErrorHandler, MimeTypes}
import play.api._
import play.api.mvc._
import play.api.routing.Router
import util.Logging

import scala.concurrent._

class ErrorHandler @Inject() (
    env: Environment, config: Configuration, sourceMapper: OptionalSourceMapper, router: Provider[Router]
) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) with Rendering with AcceptExtractors with Logging {

  override protected def onDevServerError(request: RequestHeader, ex: UsefulException) = {
    render.async {
      case Accepts.Json() => jsonError(request, ex)
      case _ => super.onDevServerError(request, ex)
    }(request)
  }

  override def onProdServerError(request: RequestHeader, ex: UsefulException) = {
    render.async {
      case Accepts.Json() => jsonError(request, ex)
      case _ => Future.successful {
        Results.InternalServerError(views.html.error.serverError(request.path, Some(ex))(request.session, request.flash))
      }
    }(request)
  }

  override def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    render.async {
      case Accepts.Json() => jsonNotFound(request, statusCode, message)
      case _ => Future.successful {
        Results.NotFound(views.html.error.notFound(request.path)(request.session, request.flash))
      }
    }(request)
  }

  override protected def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(Results.BadRequest(views.html.error.badRequest(request.path, error)(request.session, request.flash)))
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
