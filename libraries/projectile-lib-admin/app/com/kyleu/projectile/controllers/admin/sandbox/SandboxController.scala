package com.kyleu.projectile.controllers.admin.sandbox

import akka.util.Timeout
import com.google.inject.Injector
import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.module.{Application, ApplicationFeatures}
import com.kyleu.projectile.models.sandbox.SandboxTask
import com.kyleu.projectile.util.JsonSerializers._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class SandboxController @javax.inject.Inject() (
    override val app: Application, injector: Injector
)(implicit ec: ExecutionContext) extends AuthController("sandbox") {
  ApplicationFeatures.enable("sandbox")

  implicit val timeout: Timeout = Timeout(10.seconds)

  def list = withSession("list", admin = true) { implicit request => implicit td =>
    val cfg = app.cfgAdmin(u = request.identity, "system", "tools", "sandbox")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.sandbox.sandboxList(cfg)))
  }

  def run(key: String, arg: Option[String]) = withSession(key, admin = true) { implicit request => implicit td =>
    val sandbox = SandboxTask.get(key)
    sandbox.run(SandboxTask.Config(app.tracing, injector, arg)).map { result =>
      render {
        case Accepts.Html() =>
          val cfg = app.cfgAdmin(u = request.identity, "system", "tools", "sandbox")
          Ok(com.kyleu.projectile.views.html.admin.sandbox.sandboxRun(cfg, result))
        case Accepts.Json() => Ok(result.asJson)
      }
    }
  }
}
