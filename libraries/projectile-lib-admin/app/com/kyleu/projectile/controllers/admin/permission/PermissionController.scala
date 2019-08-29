package com.kyleu.projectile.controllers.admin.permission

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.controllers.admin.permission.routes.PermissionController
import com.kyleu.projectile.models.auth.Permission
import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.module.ApplicationFeature.Permission.value
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.queries.permission.PermissionQueries
import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.models.web.{ControllerUtils, InternalIcons}
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.database.JdbcDatabase
import com.kyleu.projectile.util.tracing.TraceData
import javax.inject.Named

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@javax.inject.Singleton
class PermissionController @javax.inject.Inject() (
    override val app: Application, @Named("system") db: JdbcDatabase
)(implicit ec: ExecutionContext) extends AuthController("admin.permission") {
  ApplicationFeature.enable(ApplicationFeature.Permission)
  app.errors.checkTable("system_permission")

  app.tracing.topLevelTraceBlocking("permissions") { td =>
    try { reload(td) } catch { case NonFatal(x) => log.warn(s"Error loading permissions: ${x.getMessage}")(td) }
  }
  PermissionService.registerModel("tools", "Permission", "Permission", Some(InternalIcons.permission), "view", "edit", "refresh")
  val desc = "Configure roles and permissions"
  SystemMenu.addToolMenu(value, "Permissions", Some(desc), PermissionController.list(), InternalIcons.permission, ("models", "Permission", "view"))

  def list() = withSession("list", ("tools", "Permission", "view")) { implicit request => implicit td =>
    val cfg = app.cfg(u = Some(request.identity), "system", "tools", "permission")
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.permission.permissionList(cfg, PermissionService.roles(), PermissionService.packages())))
  }

  def refresh() = withSession("refresh", ("tools", "Permission", "refresh")) { _ => td =>
    reload(td)
    val msg = "Refreshed permissions"
    Future.successful(Redirect(com.kyleu.projectile.controllers.admin.permission.routes.PermissionController.list()).flashing("success" -> msg))
  }

  def editForm() = withSession("form", ("tools", "Permission", "edit")) { implicit request => implicit td =>
    val cfg = app.cfg(u = Some(request.identity), "system", "tools", "permission", "Edit")
    val dbp = db.query(PermissionQueries.getAll(orderBys = Seq(OrderBy("role"), OrderBy("pkg"), OrderBy("model"), OrderBy("action"))))
    Future.successful(Ok(com.kyleu.projectile.views.html.admin.permission.permissionEdit(cfg, PermissionService.roles(), PermissionService.packages(), dbp)))
  }

  def delete(role: String, pkg: Option[String], model: Option[String], action: Option[String]) = {
    withSession("delete", ("tools", "Permission", "edit")) { _ => implicit td =>
      val (p, m, a) = (pkg.getOrElse(""), model.getOrElse(""), action.getOrElse(""))
      db.execute(PermissionQueries.removeByPrimaryKey(role, p, m, a))
      reload(td)
      listRedirFlashing("Deleted permission from database")
    }
  }

  def set() = withSession("set", ("tools", "Permission", "edit")) { implicit request => implicit td =>
    val form = ControllerUtils.getForm(request.body)
    def get(k: String) = form.get(k).map(_.trim).filter(_.nonEmpty)
    try {
      val role = get("role").getOrElse(throw new IllegalStateException("A role must be selected"))
      val allow = form.getOrElse("allow", "false") == "true"
      val perm = Permission(role = role, pkg = get("pkg"), model = get("model"), action = get("action"), allow = allow, createdBy = Some(request.identity.id))
      db.query(PermissionQueries.getByPrimaryKey(perm.role, perm.pkg.getOrElse(""), perm.model.getOrElse(""), perm.action.getOrElse(""))) match {
        case Some(p) if p.allow == allow => // noop
        case Some(p) =>
          val fields = Seq(DataField("allow", Some(allow.toString)))
          db.execute(PermissionQueries.update(perm.role, perm.pkg.getOrElse(""), perm.model.getOrElse(""), perm.action.getOrElse(""), fields))
        case None => db.execute(PermissionQueries.insert(perm))
      }
      reload(td)
      val msg = s"Set ${perm.key} for role [$role] to [${perm.allow}]"
      Future.successful(Redirect(com.kyleu.projectile.controllers.admin.permission.routes.PermissionController.list()).flashing("success" -> msg))
    } catch {
      case NonFatal(x) => Future.successful(
        Redirect(com.kyleu.projectile.controllers.admin.permission.routes.PermissionController.list()).flashing("error" -> x.getMessage)
      )
    }
  }

  private[this] def listRedirFlashing(msg: String, success: Boolean = true) = {
    val rsp = Redirect(com.kyleu.projectile.controllers.admin.permission.routes.PermissionController.list())
    Future.successful(rsp.flashing((if (success) { "success" } else { "error" }, msg)))
  }

  private[this] def reload(td: TraceData) = PermissionService.initialize(db.query(PermissionQueries.getAll())(td))(td)
}
