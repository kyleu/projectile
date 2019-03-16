package controllers

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.Application
import com.kyleu.projectile.models.auth.AuthActions
import com.kyleu.projectile.services.database.ApplicationDatabase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@javax.inject.Singleton
class HomeController @javax.inject.Inject() (override val app: Application, authActions: AuthActions) extends AuthController("home") {
  ApplicationDatabase.migrateSafe()

  def home() = withoutSession("home") { implicit request => implicit td =>
    Future.successful(Ok(views.html.index(request.identity)))
  }
}
