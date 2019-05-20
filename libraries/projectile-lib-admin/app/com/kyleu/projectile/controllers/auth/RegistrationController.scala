package com.kyleu.projectile.controllers.auth

import java.util.UUID

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.auth.{UserCredentials, UserForms}
import com.kyleu.projectile.models.module.Application
import com.kyleu.projectile.models.user.SystemUser
import com.kyleu.projectile.services.user.{SystemUserSearchService, SystemUserService}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, SignUpEvent}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class RegistrationController @javax.inject.Inject() (
    override val app: Application,
    userSearchService: SystemUserSearchService,
    authInfoRepository: AuthInfoRepository,
    hasher: PasswordHasher,
    userService: SystemUserService,
    configProvider: Application.UiConfigProvider
)(implicit ec: ExecutionContext) extends AuthController("registration") {
  def registrationForm(email: Option[String] = None) = withoutSession("form") { implicit request => implicit td =>
    if (configProvider.allowRegistration) {
      val username = email.map(e => if (e.contains('@')) { e.substring(0, e.indexOf('@')) } else { "" }).getOrElse("")
      val cfg = app.cfg(u = request.identity, admin = false)
      Future.successful(Ok(com.kyleu.projectile.views.html.auth.signup(username, email.getOrElse(""), cfg)))
    } else {
      Future.successful(Redirect("/").flashing("error" -> "You cannot sign up at this time, please contact your administrator"))
    }
  }

  def register = withoutSession("register") { implicit request => implicit td =>
    if (!configProvider.allowRegistration) {
      throw new IllegalStateException("You cannot sign up at this time, please contact your administrator")
    }
    UserForms.registrationForm.bindFromRequest.fold(
      form => {
        val cfg = app.cfg(u = request.identity, admin = false)
        val username = form.apply("username").value.getOrElse("")
        val email = form.apply("email").value.getOrElse("")
        Future.successful(BadRequest(com.kyleu.projectile.views.html.auth.signup(username, email, cfg)))
      },
      data => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email.toLowerCase)
        userSearchService.getByLoginInfo(loginInfo).flatMap {
          case Some(_) =>
            val redir = Redirect(com.kyleu.projectile.controllers.auth.routes.RegistrationController.registrationForm(Some(data.email)))
            Future.successful(redir.flashing("error" -> "That email address is already in use"))
          case None =>
            val authInfo = hasher.hash(data.password)
            val role = configProvider.defaultRole
            val user = SystemUser(
              id = UUID.randomUUID,
              username = data.username,
              profile = loginInfo,
              role = role
            )
            val creds = UserCredentials(user, request.remoteAddress)
            userService.insert(creds, user).flatMap { userSaved =>
              val result = request.session.get("returnUrl") match {
                case Some(url) => Redirect(url).withSession(request.session - "returnUrl")
                case None => Redirect("/")
              }
              for {
                _ <- authInfoRepository.add(loginInfo, authInfo)
                authenticator <- app.silhouette.env.authenticatorService.create(loginInfo)
                value <- app.silhouette.env.authenticatorService.init(authenticator)
                result <- app.silhouette.env.authenticatorService.embed(value, result)
              } yield {
                app.silhouette.env.eventBus.publish(SignUpEvent(userSaved, request))
                app.silhouette.env.eventBus.publish(LoginEvent(userSaved, request))
                result.flashing("success" -> "You're all set!")
              }
            }
        }
      }
    )
  }
}
