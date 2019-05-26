package com.kyleu.projectile.web.controllers.project

import com.kyleu.projectile.models.feature.ServiceFeature
import com.kyleu.projectile.models.project.member.{MemberOverride, ServiceMember}
import com.kyleu.projectile.util.StringUtils
import com.kyleu.projectile.web.controllers.ProjectileController
import com.kyleu.projectile.web.util.ControllerUtils

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectServiceController @javax.inject.Inject() () extends ProjectileController {
  def detail(key: String, service: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val i = p.getInput

    val s = p.getService(service)
    val em = i.service(service)
    val updated = em.apply(s)
    val fin = updated.copy(methods = em.methods.map(m => updated.getMethodOpt(m.key).getOrElse(m)))

    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.member.detailService(projectile, key, p.toSummary, s, fin)))
  }

  def formNew(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val i = p.getInput

    val inputServices = i.services.map(s => (s.key, p.services.exists(x => x.key == s.key)))
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.member.formNewService(projectile, key, inputServices)))
  }

  def add(key: String, serviceKey: String) = Action.async { _ =>
    val p = projectile.getProject(key)
    val i = p.getInput
    serviceKey match {
      case "all" =>
        val toSave = i.services.flatMap {
          case m if p.getServiceOpt(m.key).isDefined => None
          case m => Some(ServiceMember(pkg = m.pkg, key = m.key, features = p.defaultServiceFeatures.map(ServiceFeature.withValue)))
        }
        val saved = projectile.saveServiceMembers(key, toSave)
        val redir = Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectController.detail(key))
        Future.successful(redir.flashing("success" -> s"Added ${saved.size} services"))
      case _ =>
        val orig = i.services.find(_.key == serviceKey).getOrElse(throw new IllegalStateException(s"Cannot find service [$serviceKey]"))
        val m = ServiceMember(pkg = orig.pkg, key = serviceKey, features = p.defaultServiceFeatures.map(ServiceFeature.withValue))
        projectile.saveServiceMember(key, m)
        val redir = Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectServiceController.detail(key, m.key))
        Future.successful(redir.flashing("success" -> s"Added service [${m.key}]"))
    }
  }

  def save(key: String, serviceKey: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val i = p.getInput

    val m = p.getService(serviceKey)
    val svc = i.service(m.key)

    val form = ControllerUtils.getForm(request.body)

    val nameOverrides = Seq(
      form("propertyName") match {
        case x if x.nonEmpty && x != svc.propertyName => Some(MemberOverride("propertyName", x))
        case _ => None
      },
      form("className") match {
        case x if x.nonEmpty && x != svc.className => Some(MemberOverride("className", x))
        case _ => None
      }
    ).flatten

    val methodOverrides = svc.methods.flatMap { _ =>
      Nil // TODO
    }

    val newMember = m.copy(
      pkg = StringUtils.toList(form("package"), '.'),
      features = StringUtils.toList(form.getOrElse("features", "")).map(ServiceFeature.withValue).toSet,
      ignored = StringUtils.toList(form.getOrElse("ignored", "")).toSet,
      overrides = nameOverrides ++ methodOverrides
    )

    projectile.saveServiceMembers(key, Seq(newMember))
    val redir = Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectServiceController.detail(key, serviceKey))
    Future.successful(redir.flashing("success" -> s"Saved service [$serviceKey]"))
  }

  def remove(key: String, member: String) = Action.async { _ =>
    projectile.removeServiceMember(key, member)
    Future.successful(Redirect(
      com.kyleu.projectile.web.controllers.project.routes.ProjectController.detail(key)
    ).flashing("success" -> s"Removed service [$member]"))
  }

  def formFeatures(key: String) = Action.async { implicit request =>
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.form.formServiceFeatures(projectile, projectile.getProjectSummary(key))))
  }

  def saveFeatures(key: String) = Action.async { implicit request =>
    val form = ControllerUtils.getForm(request.body)
    val summary = projectile.getProjectSummary(key)
    val features = StringUtils.toList(form("features")).map(ServiceFeature.withValue).map(_.value).toSet
    projectile.saveProject(summary.copy(defaultServiceFeatures = features))

    Future.successful(Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectController.detail(summary.key)).flashing(
      "success" -> s"Saved default service features for project [${summary.key}]"
    ))
  }
}
