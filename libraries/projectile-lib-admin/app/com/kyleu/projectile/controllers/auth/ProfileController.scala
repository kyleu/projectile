package com.kyleu.projectile.controllers.auth

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.Application
import com.kyleu.projectile.models.auth.{AuthActions, UserForms}
import com.kyleu.projectile.models.user.UserProfile
import com.kyleu.projectile.services.user.SystemUserService
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.web.util.ControllerUtils
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasher}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import io.circe.JsonObject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@javax.inject.Singleton
class ProfileController @javax.inject.Inject() (
    override val app: Application,
    authInfoRepository: AuthInfoRepository,
    credentialsProvider: CredentialsProvider,
    hasher: PasswordHasher,
    userService: SystemUserService,
    actions: AuthActions
) extends AuthController("profile") {
  def view = withSession("view") { implicit request => implicit td =>
    Future.successful(render {
      case Accepts.Html() => Ok(actions.profile(request.identity, app.cfg(u = Some(request.identity), admin = false, "profile")))
      case Accepts.Json() => Ok(UserProfile.fromUser(request.identity).asJson)
    })
  }

  def save = withSession("view") { implicit request => implicit td =>
    UserForms.profileForm.bindFromRequest.fold(
      _ => Future.successful(BadRequest(actions.profile(request.identity, app.cfg(u = Some(request.identity), admin = false, "profile")))),
      profileData => {
        val settings = JsonObject.apply("theme" -> profileData.theme.asJson).asJson
        val newUser = request.identity.copy(username = profileData.username, settings = settings)
        userService.updateUser(request, newUser).map { _ =>
          Redirect(actions.indexUrl)
        }
      }
    )
  }

  def changePasswordForm = withSession("change-password-form") { implicit request => implicit td =>
    Future.successful(Ok(actions.changePasswordForm(request.identity)))
  }

  def changePassword = withSession("change-password") { implicit request => implicit td =>
    def errorResponse(msg: String) = Redirect(actions.changePasswordUrl).flashing("error" -> msg)
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
            val okResponse = Redirect(actions.profileUrl).flashing("success" -> "Password changed")
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
