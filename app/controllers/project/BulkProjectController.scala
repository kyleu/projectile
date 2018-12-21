package controllers.project

import com.kyleu.projectile.models.feature.{EnumFeature, ModelFeature, ServiceFeature}
import com.kyleu.projectile.models.project.member.MemberOverride
import controllers.BaseController
import util.web.ControllerUtils

import scala.concurrent.Future

@javax.inject.Singleton
class BulkProjectController @javax.inject.Inject() () extends BaseController {
  def updateAll() = Action.async { implicit request =>
    val result = projectile.listProjects().flatMap(project => projectile.updateProject(key = project.key))
    Future.successful(Redirect(controllers.routes.HomeController.index()).flashing("success" -> result.mkString(", ")))
  }

  def exportAll() = Action.async { implicit request =>
    val result = projectile.listProjects().map(project => projectile.exportProject(key = project.key, verbose = false))
    Future.successful(Ok(views.html.project.outputResults(projectile = projectile, outputs = result.map(_._1), files = result.flatMap(_._2), verbose = false)))
  }

  def auditAll() = Action.async { implicit request =>
    val result = projectile.audit(keys = projectile.listProjects().map(_.key), verbose = false)
    Future.successful(Ok(views.html.project.audit.auditResult(projectile, result)))
  }

  def enumBulkEditForm(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    Future.successful(Ok(views.html.project.form.formEnumMembers(projectile, p)))
  }

  private[this] def overridesFor(overrides: Seq[MemberOverride], k: String, form: Map[String, String]) = {
    overrides.filterNot(o => o.k == "propertyName" || o.k == "className") ++
      form.get(s"$k-propertyName").filter(_.trim.nonEmpty).map(v => MemberOverride("propertyName", v)).toSeq ++
      form.get(s"$k-className").filter(_.trim.nonEmpty).map(v => MemberOverride("className", v)).toSeq
  }

  def enumBulkEdit(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val form = ControllerUtils.getForm(request.body)
    val memberKeys = form.keys.filter(_.endsWith("-package")).map(_.stripSuffix("-package")).toSeq.sorted
    val newMembers = memberKeys.map { k =>
      val e = p.getEnum(k)
      val pkg = form(s"$k-package").split('.').map(_.trim).filter(_.nonEmpty)
      val features = form.keys.filter(_.startsWith(s"$k-feature-")).map(_.stripPrefix(s"$k-feature-")).map(EnumFeature.withValue).toSet
      e.copy(pkg = pkg, features = features, overrides = overridesFor(e.overrides, k, form))
    }
    val updated = projectile.saveEnumMembers(key, newMembers)
    val msg = s"Saved [${updated.size}] project enum members"
    Future.successful(Redirect(controllers.project.routes.ProjectController.detail(key)).flashing("success" -> msg))
  }

  def modelBulkEditForm(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    Future.successful(Ok(views.html.project.form.formModelMembers(projectile, p)))
  }

  def modelBulkEdit(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val form = ControllerUtils.getForm(request.body)
    val memberKeys = form.keys.filter(_.endsWith("-package")).map(_.stripSuffix("-package")).toSeq.sorted
    val newMembers = memberKeys.map { k =>
      val m = p.getModel(k)
      val pkg = form(s"$k-package").split('.').map(_.trim).filter(_.nonEmpty)
      val features = form.keys.filter(_.startsWith(s"$k-feature-")).map(_.stripPrefix(s"$k-feature-")).map(ModelFeature.withValue).toSet
      m.copy(pkg = pkg, features = features, overrides = overridesFor(m.overrides, k, form))
    }
    val updated = projectile.saveModelMembers(key, newMembers)
    val msg = s"Saved [${updated.size}] project model members"
    Future.successful(Redirect(controllers.project.routes.ProjectController.detail(key)).flashing("success" -> msg))
  }

  def serviceBulkEditForm(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    Future.successful(Ok(views.html.project.form.formServiceMembers(projectile, p)))
  }

  def serviceBulkEdit(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val form = ControllerUtils.getForm(request.body)
    val memberKeys = form.keys.filter(_.endsWith("-package")).map(_.stripSuffix("-package")).toSeq.sorted
    val newMembers = memberKeys.map { k =>
      val s = p.getService(k)
      val pkg = form(s"$k-package").split('.').map(_.trim).filter(_.nonEmpty)
      val features = form.keys.filter(_.startsWith(s"$k-feature-")).map(_.stripPrefix(s"$k-feature-")).map(ServiceFeature.withValue).toSet
      s.copy(pkg = pkg, features = features, overrides = overridesFor(s.overrides, k, form))
    }
    val updated = projectile.saveServiceMembers(key, newMembers)
    val msg = s"Saved [${updated.size}] project model members"
    Future.successful(Redirect(controllers.project.routes.ProjectController.detail(key)).flashing("success" -> msg))
  }
}
