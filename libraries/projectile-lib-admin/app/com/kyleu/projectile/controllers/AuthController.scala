// scalastyle:off file.size.limit
package com.kyleu.projectile.controllers

import com.kyleu.projectile.models.auth.{AuthEnv, UserCredentials}
import com.kyleu.projectile.models.module.Application
import com.kyleu.projectile.models.user.SystemUser
import com.kyleu.projectile.models.web.StartupErrorFixes
import com.kyleu.projectile.util.Credentials
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.util.metrics.Instrumented
import com.kyleu.projectile.util.tracing.TraceData
import com.mohiva.play.silhouette.api.actions.{SecuredRequest, UserAwareRequest}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

abstract class AuthController(name: String) extends com.kyleu.projectile.controllers.BaseController(name) {
  type Req = SecuredRequest[AuthEnv, AnyContent]

  def app: Application
  override def tracing = app.tracing

  private[this] def appErrorsOr(f: () => Action[AnyContent]): Action[AnyContent] = if (app.errors.hasErrors) { appErrors() } else { f() }

  protected implicit def toCredentials(request: SecuredRequest[AuthEnv, _]): UserCredentials = UserCredentials.fromRequest(request)
  protected implicit def toOptionalCredentials(r: UserAwareRequest[AuthEnv, _]): Credentials = {
    UserCredentials.fromInsecureRequest(r).getOrElse(Credentials.anonymous)
  }

  protected def withoutSession(action: String)(block: UserAwareRequest[AuthEnv, AnyContent] => TraceData => Future[Result])(implicit ec: ExecutionContext) = {
    appErrorsOr { () =>
      app.silhouette.UserAwareAction.async { implicit request =>
        Instrumented.timeFuture(metricsName + "_request", "action", name + "_" + action) {
          app.tracing.trace(name + ".controller." + action) { td =>
            enhanceRequest(request, request.identity.map(_.user), td)
            block(request)(td)
          }(getTraceData)
        }
      }
    }
  }

  protected def withSession(action: String, permissions: (String, String, String)*)(
    block: Req => TraceData => Future[Result]
  )(implicit ec: ExecutionContext) = {
    appErrorsOr { () =>
      app.silhouette.UserAwareAction.async { implicit request =>
        request.identity match {
          case Some(u) => permissions.map(p => PermissionService.check(u.role, p._1, p._2, p._3)).filter(!_._1).map(_._2).toList match {
            case Nil => Instrumented.timeFuture(metricsName + "_request", "action", name + "_" + action) {
              app.tracing.trace(name + ".controller." + action) { td =>
                enhanceRequest(request, Some(u.user), td)
                val auth = request.authenticator.getOrElse(throw new IllegalStateException("No auth!"))
                block(SecuredRequest(u, auth, request))(td)
              }(getTraceData)
            }
            case x =>
              val msg = permissions.map(p => s"${p._1}.${p._2}.${p._3}").mkString(", ")
              Future.successful(Redirect("/").flashing("error" -> s"You are not authorized to access that page ($msg)"))
          }
          case None => failRequest(request)
        }
      }
    }
  }

  protected def failRequest(request: UserAwareRequest[AuthEnv, AnyContent]) = {
    val msg = request.identity match {
      case Some(_) => "You do not have sufficient permissions to access that"
      case None => s"You must sign in or register before accessing this application"
    }
    val redir = Redirect(com.kyleu.projectile.controllers.auth.routes.AuthenticationController.signInForm())
    Future.successful(redir.flashing("error" -> msg.take(1024)).withSession(request.session + ("returnUrl" -> request.uri)))
  }

  protected def enhanceRequest(request: Request[AnyContent], user: Option[SystemUser], trace: TraceData) = {
    super.enhanceRequest(request, trace)
    user.foreach { u =>
      trace.tag("user.id", u.id.toString)
      trace.tag("user.username", u.username)
      trace.tag("user.email", u.profile.providerKey)
      trace.tag("user.role", u.role.toString)
    }
  }

  private[this] def appErrors() = Action.async { implicit r =>
    def reload(td: TraceData) = if (app.reload(td)) {
      Future.successful(Redirect("/"))
    } else {
      Future.successful(Ok(com.kyleu.projectile.views.html.error.startupError(app)))
    }
    app.tracing.topLevelTraceBlocking("reload") { td =>
      if (r.queryString.get("errors").exists(_.headOption.contains("reset"))) {
        reload(td)
      } else {
        r.queryString.get("fix").map(_.headOption.getOrElse(throw new IllegalStateException())) match {
          case Some(fix) =>
            StartupErrorFixes.fix(app, fix)
            reload(td)
          case None => Future.successful(Ok(com.kyleu.projectile.views.html.error.startupError(app)))
        }
      }
    }
  }
}
