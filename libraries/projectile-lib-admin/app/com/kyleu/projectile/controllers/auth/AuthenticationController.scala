package com.kyleu.projectile.controllers.auth

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.Application
import com.kyleu.projectile.models.auth.{AuthActions, UserForms}
import com.kyleu.projectile.services.user.SystemUserSearchService
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.{LoginEvent, LogoutEvent}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.mvc.Result

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class AuthenticationController @javax.inject.Inject() (
    override val app: Application,
    userSearchService: SystemUserSearchService,
    credentialsProvider: CredentialsProvider,
    actions: AuthActions
)(implicit ec: ExecutionContext) extends AuthController("authentication") {
  val providers = if (app.config.authGoogleSettings.clientSecret.nonEmpty) { Seq("google") } else { Nil }

  def signInForm = withoutSession("form") { implicit request => implicit td =>
    val resp = Ok(actions.signin(UserForms.signInForm, app.cfg(u = request.identity, admin = false)))
    Future.successful(resp)
  }

  def authenticateCredentials = withoutSession("authenticate") { implicit request => implicit td =>
    UserForms.signInForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(actions.signin(form, app.cfg(u = request.identity, admin = false)))),
      credentials => {
        val creds = credentials.copy(identifier = credentials.identifier.toLowerCase)
        credentialsProvider.authenticate(creds).flatMap { loginInfo =>
          val result = request.session.get("returnUrl") match {
            case Some(url) => Redirect(url).withSession(request.session - "returnUrl")
            case None => Redirect(actions.indexUrl)
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
            Redirect(actions.signinUrl).flashing("error" -> msg)
        }
      }
    )
  }

  def signOut = withSession("signout") { implicit request => implicit td =>
    implicit val result: Result = Redirect(actions.indexUrl)
    app.silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    app.silhouette.env.authenticatorService.discard(request.authenticator, result)
  }
}
