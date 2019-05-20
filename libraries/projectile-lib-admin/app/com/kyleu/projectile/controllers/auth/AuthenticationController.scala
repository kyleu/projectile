package com.kyleu.projectile.controllers.auth

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.auth.UserForms
import com.kyleu.projectile.models.config.UiConfig
import com.kyleu.projectile.models.module.Application
import com.kyleu.projectile.services.user.SystemUserSearchService
import com.kyleu.projectile.util.tracing.TraceData
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api.{LoginEvent, LogoutEvent}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.data.Form
import play.api.mvc.{Flash, Result, Session}
import play.twirl.api.HtmlFormat

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class AuthenticationController @javax.inject.Inject() (
    override val app: Application,
    userSearchService: SystemUserSearchService,
    credentialsProvider: CredentialsProvider
)(implicit ec: ExecutionContext) extends AuthController("authentication") {
  val providers = if (app.config.authGoogleSettings.clientSecret.nonEmpty) { Seq("google") } else { Nil }

  def signInForm = withoutSession("form") { implicit request => implicit td =>
    val resp = Ok(signin(UserForms.signInForm, app.cfg(u = request.identity, admin = false)))
    Future.successful(resp)
  }

  def authenticateCredentials = withoutSession("authenticate") { implicit request => implicit td =>
    UserForms.signInForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(signin(form, app.cfg(u = request.identity, admin = false)))),
      credentials => {
        val creds = credentials.copy(identifier = credentials.identifier.toLowerCase)
        credentialsProvider.authenticate(creds).flatMap { loginInfo =>
          val result = request.session.get("returnUrl") match {
            case Some(url) => Redirect(url).withSession(request.session - "returnUrl")
            case None => Redirect("/")
          }
          userSearchService.getByLoginInfo(loginInfo).flatMap {
            case Some(user) => app.silhouette.env.authenticatorService.create(loginInfo).flatMap { authenticator =>
              app.silhouette.env.eventBus.publish(LoginEvent(user, request))
              app.silhouette.env.authenticatorService.init(authenticator).flatMap { v =>
                app.silhouette.env.authenticatorService.embed(v, result).map { x =>
                  log.info(s"Successful sign in for [${credentials.identifier}]")
                  x
                }
              }
            }
            case None =>
              log.warn(s"Couldn't find user matching [${credentials.identifier}]")
              Future.failed(new IdentityNotFoundException(s"Couldn't find user [${loginInfo.providerID}]"))
          }
        }.recover {
          case _: ProviderException =>
            val msg = "Invalid credentials"
            log.warn(msg + " for [" + credentials.identifier + "]")
            Redirect(com.kyleu.projectile.controllers.auth.routes.AuthenticationController.signInForm()).flashing("error" -> msg)
        }
      }
    )
  }

  def signOut = withSession("signout") { implicit request => implicit td =>
    implicit val result: Result = Redirect("/")
    app.silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    app.silhouette.env.authenticatorService.discard(request.authenticator, result)
  }

  private[this] def signin(
    form: Form[Credentials], cfg: UiConfig
  )(implicit session: Session, flash: Flash, traceData: TraceData): HtmlFormat.Appendable = {
    val username = form.apply("identifier").value.getOrElse("")
    com.kyleu.projectile.views.html.auth.signin(username, cfg)
  }
}
