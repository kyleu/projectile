package controllers.project

import com.projectile.models.database.input.PostgresInput
import com.projectile.models.feature.ServiceFeature
import com.projectile.models.project.member.ServiceMember.InputType
import com.projectile.models.project.member.{MemberOverride, ServiceMember}
import com.projectile.models.thrift.input.ThriftInput
import controllers.BaseController
import util.web.ControllerUtils

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectServiceController @javax.inject.Inject() () extends BaseController {
  def detail(key: String, model: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val m = p.getService(model)

    val i = projectile.getInput(m.input)
    val em = i.exportService(model)
    val updated = em.apply(m)
    val fin = updated.copy(methods = em.methods.map(m => updated.getMethodOpt(m).getOrElse(m)))

    Future.successful(Ok(views.html.project.member.detailService(projectile, key, p.toSummary, m, fin)))
  }

  def formNew(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val inputServices = projectile.listInputs().map { input =>
      input.key -> (projectile.getInput(input.key) match {
        case pi: PostgresInput => Nil
        case ti: ThriftInput => ti.services.map(s => (s.key, InputType.ThriftService.value, p.services.exists(x => x.input == ti.key && x.key == s.key)))
        case x => throw new IllegalStateException(s"Unhandled input [$x]")
      })
    }
    Future.successful(Ok(views.html.project.member.formNewService(projectile, key, inputServices)))
  }

  def add(key: String, input: String, inputType: String, inputKey: String) = Action.async { implicit request =>
    val i = projectile.getInput(input)
    val p = projectile.getProject(key)
    inputKey match {
      case "all" =>
        val toSave = i.exportServices.flatMap {
          case m if p.getServiceOpt(m.key).isDefined => None
          case m => Some(ServiceMember(input = input, pkg = m.pkg, inputType = m.inputType, key = m.key, features = p.serviceFeatures.toSet))
        }
        val saved = projectile.saveServiceMembers(key, toSave)
        val redir = Redirect(controllers.project.routes.ProjectController.detail(key))
        Future.successful(redir.flashing("success" -> s"Added ${saved.size} services"))
      case _ =>
        val orig = i.exportServices.find(_.key == inputKey).getOrElse(throw new IllegalStateException(s"Cannot find service [$inputKey] in input [$input]"))
        val it = ServiceMember.InputType.withValue(inputType)
        val m = ServiceMember(input = input, pkg = orig.pkg, inputType = it, key = inputKey, features = p.serviceFeatures.toSet)
        projectile.saveServiceMembers(key, Seq(m))
        val redir = Redirect(controllers.project.routes.ProjectServiceController.detail(key, m.key))
        Future.successful(redir.flashing("success" -> s"Added service [${m.key}]"))
    }
  }

  def save(key: String, serviceKey: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val m = p.getService(serviceKey)

    val i = projectile.getInput(m.input)
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

    val methodOverrides = svc.methods.flatMap { m =>
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
