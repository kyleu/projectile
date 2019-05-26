package com.kyleu.projectile.controllers.admin.sandbox

import akka.util.Timeout
import com.google.inject.Injector
import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.controllers.admin.sandbox.routes.SandboxController
import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.module.ApplicationFeature.Sandbox.value
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.sandbox.SandboxTask
import com.kyleu.projectile.models.web.InternalIcons
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.util.JsonSerializers._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class SandboxController @javax.inject.Inject() (
    override val app: Application, injector: Injector
)(implicit ec: ExecutionContext) extends AuthController("sandbox") {
  ApplicationFeature.enable(ApplicationFeature.Sandbox)
  PermissionService.registerModel("tools", "Sandbox", "Sandbox Actions", Some(InternalIcons.sandbox), "view", "run")
  SystemMenu.addToolMenu(value, "Sandbox Tasks", Some("Simple one-off tasks that can be run through this UI"), SandboxController.list(), InternalIcons.sandbox)

  implicit val timeout: Timeout = Timeout(10.seconds)

  def list = withSession("list", ("tools", "Sandbox", "view")) { implicit request => _ =>
    val cfg = app.cfg(u = Some(request.identity), "system", "tools", "sandbox")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.sandbox.sandboxList(cfg)))
  }

  def run(key: String, arg: Option[String]) = withSession(key, ("tools", "Sandbox", "run")) { implicit request => implicit td =>
    val sandbox = SandboxTask.get(key)
    sandbox.run(SandboxTask.Config(app.tracing, injector, arg)).map { result =>
      render {
        case Accepts.Html() =>
          val cfg = app.cfg(u = Some(request.identity), "system", "tools", "sandbox")
          Ok(com.kyleu.projectile.views.html.admin.sandbox.sandboxRun(cfg, result))
        case Accepts.Json() => Ok(result.asJson)
      }
    }
  }
}
