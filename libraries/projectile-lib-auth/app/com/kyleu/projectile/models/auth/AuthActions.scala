package com.kyleu.projectile.models.auth

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
  AuthActions.inst = Some(this)

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
  def adminMenu: (SystemUser, String, AuthActions) => HtmlFormat.Appendable = com.kyleu.projectile.views.html.admin.layout.defaultMenu.apply

  def signin(
    user: Option[SystemUser], form: Form[Credentials], providers: scala.Seq[String], allowRegistration: Boolean
  )(implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: TraceData): HtmlFormat.Appendable = {
    com.kyleu.projectile.views.html.auth.signin(user, form, providers, allowRegistration, this)
  }

  def registerForm(
    u: Option[SystemUser], f: Form[RegistrationData]
  )(implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: TraceData): HtmlFormat.Appendable = {
    com.kyleu.projectile.views.html.auth.register(u, f, this)
  }

  def profile(u: SystemUser)(implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: TraceData): HtmlFormat.Appendable = {
    com.kyleu.projectile.views.html.profile.view(u, this)
  }

  def changePasswordForm(u: SystemUser)(implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: TraceData): HtmlFormat.Appendable = {
    com.kyleu.projectile.views.html.profile.changePassword(u, this)
  }
}
