package com.kyleu.projectile.models.auth

import com.kyleu.projectile.models.config.{NavUrls, UiConfig}
import com.kyleu.projectile.models.user.{Role, SystemUser}
import com.kyleu.projectile.util.tracing.TraceData
import com.mohiva.play.silhouette.api.util.Credentials
import play.api.data.Form
import play.api.mvc.{AnyContent, Flash, Request, Session}
import play.twirl.api.HtmlFormat

object AuthActions {
  private var inst: Option[AuthActions] = None

  def getInst = inst.getOrElse(throw new IllegalStateException("AuthActions has not been initialized"))
}

class AuthActions(val projectName: String) {
  def allowRegistration = true
  def defaultRole: Role = Role.User

  def indexUrl = "/"

  def registerUrl = "/profile/signup"
  def signinUrl = "/profile/signin"
  def signoutUrl = "/profile/signout"
  def signinUrlForProvider(p: String) = s"/profile/signin/$p"

  def profileUrl = "/profile"
  def changePasswordUrl = "/profile/password/change"

  def adminIndexUrl = "/admin/system"

  def signin(
    form: Form[Credentials], cfg: UiConfig
  )(implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: TraceData): HtmlFormat.Appendable = {
    val username = form.apply("identifier").value.getOrElse("")
    com.kyleu.projectile.components.views.html.auth.signin(username, cfg)
  }

  def registerForm(
    f: Form[RegistrationData], cfg: UiConfig
  )(implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: TraceData): HtmlFormat.Appendable = {
    val username = f.apply("username").value.getOrElse("")
    val email = f.apply("email").value.getOrElse("")
    com.kyleu.projectile.components.views.html.auth.signup(username, email, cfg)
  }

  def profile(cfg: UiConfig)(implicit request: Request[AnyContent], session: Session, flash: Flash): HtmlFormat.Appendable = {
    com.kyleu.projectile.views.html.profile.view(cfg)
  }

  def changePasswordForm(cfg: UiConfig)(implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: TraceData): HtmlFormat.Appendable = {
    com.kyleu.projectile.components.views.html.auth.changePassword(cfg)
  }
}
