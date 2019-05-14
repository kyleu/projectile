package controllers

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.Application

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class HomeController @javax.inject.Inject() (override val app: Application)(implicit ec: ExecutionContext) extends AuthController("home") {
  def home() = withSession("home") { implicit request => implicit td =>
    Future.successful(Ok(views.html.index(request.identity, app.cfg(Some(request.identity), admin = false), app.config.debug)))
  }

  def sandbox() = withSession("home") { implicit request => implicit td =>
    Future.successful(Ok(views.html.sandbox(request.identity, app.cfg(Some(request.identity), admin = false))))
    Future.successful(Ok(com.kyleu.projectile.views.html.error.serverError("", Some(new IllegalStateException("xxx")))))
  }

  def redir(file: String) = withoutSession("redir") { implicit request => implicit td =>
    Future.successful(Redirect(controllers.routes.Assets.versioned(file)))
  }
}
