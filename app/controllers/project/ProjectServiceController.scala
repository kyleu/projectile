package controllers.project

import com.projectile.models.feature.ServiceFeature
import com.projectile.models.project.member.{MemberOverride, ServiceMember}
import controllers.BaseController
import util.web.ControllerUtils

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectServiceController @javax.inject.Inject() () extends BaseController {
  def detail(key: String, service: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val i = projectile.getInput(p.input)

    val s = p.getService(service)
    val em = i.exportService(service)
    val updated = em.apply(s)
    val fin = updated.copy(methods = em.methods.map(m => updated.getMethodOpt(m.key).getOrElse(m)))

    Future.successful(Ok(views.html.project.member.detailService(projectile, key, p.toSummary, s, fin)))
  }

  def formNew(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val i = projectile.getInput(p.input)

    val inputServices = i.exportServices.map(s => (s.key, p.services.exists(x => x.key == s.key)))
    Future.successful(Ok(views.html.project.member.formNewService(projectile, key, inputServices)))
  }

  def add(key: String, serviceKey: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val i = projectile.getInput(p.input)
    serviceKey match {
      case "all" =>
        val toSave = i.exportServices.flatMap {
          case m if p.getServiceOpt(m.key).isDefined => None
          case m => Some(ServiceMember(pkg = m.pkg, key = m.key, features = p.serviceFeatures.toSet))
        }
        val saved = projectile.saveServiceMembers(key, toSave)
        val redir = Redirect(controllers.project.routes.ProjectController.detail(key))
        Future.successful(redir.flashing("success" -> s"Added ${saved.size} services"))
      case _ =>
        val orig = i.exportServices.find(_.key == serviceKey).getOrElse(throw new IllegalStateException(s"Cannot find service [$serviceKey]"))
        val m = ServiceMember(pkg = orig.pkg, key = serviceKey, features = p.serviceFeatures.toSet)
        projectile.saveServiceMember(key, m)
        val redir = Redirect(controllers.project.routes.ProjectServiceController.detail(key, m.key))
        Future.successful(redir.flashing("success" -> s"Added service [${m.key}]"))
    }
  }

  def save(key: String, serviceKey: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val i = projectile.getInput(p.input)

    val m = p.getService(serviceKey)
    val svc = i.exportService(m.key)

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
      pkg = form("package").split('.').map(_.trim).filter(_.nonEmpty),
      features = form.getOrElse("features", "").split(',').map(_.trim).filter(_.nonEmpty).map(ServiceFeature.withValue).toSet,
      ignored = form.getOrElse("ignored", "").split(',').map(_.trim).filter(_.nonEmpty).toSet,
      overrides = nameOverrides ++ methodOverrides
    )

    projectile.saveServiceMembers(key, Seq(newMember))
    val redir = Redirect(controllers.project.routes.ProjectModelController.detail(key, serviceKey))
    Future.successful(redir.flashing("success" -> s"Saved model [$serviceKey]"))
  }

  def remove(key: String, member: String) = Action.async { implicit request =>
    projectile.removeModelMember(key, member)
    Future.successful(Redirect(controllers.project.routes.ProjectController.detail(key)).flashing("success" -> s"Removed model [$member]"))
  }
}
