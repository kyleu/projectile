package com.kyleu.projectile.controllers.auth

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.controllers.auth.routes.ProfileController
import com.kyleu.projectile.models.auth.UserForms
import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.module.ApplicationFeature.Profile.value
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.user.UserProfile
import com.kyleu.projectile.services.user.SystemUserService
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.models.web.{ControllerUtils, InternalIcons}
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasher}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class ProfileController @javax.inject.Inject() (
    override val app: Application,
    authInfoRepository: AuthInfoRepository,
    credentialsProvider: CredentialsProvider,
    hasher: PasswordHasher,
    userService: SystemUserService
)(implicit ec: ExecutionContext) extends AuthController("profile") {
  ApplicationFeature.enable(ApplicationFeature.Profile)
  app.errors.checkTable("system_user")
  SystemMenu.addRootMenu(value, "Profile", Some("View your system profile"), ProfileController.view(), InternalIcons.systemUser)

  def view(thm: Option[String]) = withSession("view") { implicit request => implicit td =>
    Future.successful(render {
      case Accepts.Html() =>
        val cfg = app.cfg(u = Some(request.identity), "system", "profile", request.identity.profile.providerKey)
        Ok(com.kyleu.projectile.views.html.auth.profile(request.identity.username, cfg.copy(user = cfg.user.copy(theme = thm.getOrElse(cfg.user.theme))), Nil))
      case Accepts.Json() => Ok(UserProfile.fromUser(request.identity.user).asJson)
    })
  }

  def save = withSession("view") { implicit request => implicit td =>
    UserForms.profileForm.bindFromRequest.fold(
      form => Future.successful {
        val cfg = app.cfg(u = Some(request.identity), "system", "profile", request.identity.profile.providerKey)
        val errors = form.errors.map(e => e.key -> e.message)
        BadRequest(com.kyleu.projectile.views.html.auth.profile(request.identity.username, cfg, errors))
      },
      profileData => {
        val settings = profileData.settings.asJson
        val newUser = request.identity.copy(user = request.identity.user.copy(username = profileData.username, settings = settings))
        userService.updateUser(request, newUser.user).map { _ =>
          Redirect(com.kyleu.projectile.controllers.auth.routes.ProfileController.view())
          // throw new IllegalStateException(settings.spaces2)
        }
      }
    )
  }

  def changePasswordForm = withSession("change-password-form") { implicit request => implicit td =>
    val cfg = app.cfg(u = Some(request.identity), "system", "profile")
    Future.successful(Ok(com.kyleu.projectile.views.html.auth.changePassword(cfg)))
  }

  def changePassword = withSession("change-password") { implicit request => _ =>
    def errorResponse(msg: String) = {
      Redirect(com.kyleu.projectile.controllers.auth.routes.ProfileController.changePasswordForm()).flashing("error" -> msg)
    }
    UserForms.changePasswordForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(errorResponse(ControllerUtils.errorsToString(formWithErrors.errors)))
      },
      changePass => {
        if (changePass.newPassword != changePass.confirm) {
          Future.successful(errorResponse("Passwords do not match"))
        } else {
          val email = request.identity.profile.providerKey
          credentialsProvider.authenticate(Credentials(email, changePass.oldPassword)).flatMap { loginInfo =>
            val okResponse = Redirect(com.kyleu.projectile.controllers.auth.routes.ProfileController.view()).flashing("success" -> "Password changed")
            for {
              _ <- authInfoRepository.update(loginInfo, hasher.hash(changePass.newPassword))
              authenticator <- app.silhouette.env.authenticatorService.create(loginInfo)
              result <- app.silhouette.env.authenticatorService.renew(authenticator, okResponse)
            } yield result
          }.recover {
            case e: ProviderException => errorResponse(s"Old password does not match (${e.getMessage}).")
          }
        }
      }
    )
  }
}
