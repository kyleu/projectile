package controllers

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.menu.NavMenu
import com.kyleu.projectile.models.module.Application

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class HomeController @javax.inject.Inject() (override val app: Application)(implicit ec: ExecutionContext) extends AuthController("home") {
  def home() = withSession("home") { implicit request => implicit td =>
    Future.successful(Ok(views.html.index(app.cfg(Some(request.identity), admin = false), app.config.debug)))
  }

  def testbed() = withSession("testbed") { implicit request => implicit td =>
    Future.successful(Ok(views.html.testbed(app.cfg(Some(request.identity), admin = false, "testbed"))))
  }

  def redir(file: String) = withoutSession("redir") { implicit request => implicit td =>
    Future.successful(Redirect(controllers.routes.Assets.versioned(file)))
  }
}
