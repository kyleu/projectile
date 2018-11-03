package controllers.project

import controllers.BaseController
import models.database.input.PostgresInput
import models.output.feature.Feature
import models.project.member.{MemberOverride, ProjectMember}
import models.project.member.ProjectMember.InputType
import util.web.ControllerUtils

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectEnumController @javax.inject.Inject() () extends BaseController {
  def detail(key: String, enum: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val m = p.getEnum(enum)

    val i = projectile.getInput(m.input)
    val ee = i.exportEnum(enum)
    val fin = ee.apply(m).copy(values = ee.values)

    Future.successful(Ok(views.html.project.member.detailEnum(projectile, key, p.toSummary, m, fin)))
  }

  def formNew(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val inputEnums = projectile.listInputs().map { input =>
      input.key -> (projectile.getInput(input.key) match {
        case pi: PostgresInput => pi.enums.map(e => (e.key, InputType.PostgresEnum.value, p.enums.exists(x => x.input == pi.key && x.key == e.key)))
        case x => throw new IllegalStateException(s"Unhandled input [$x]")
      })
    }
    Future.successful(Ok(views.html.project.member.formNewEnum(projectile, key, inputEnums)))
  }

  def add(key: String, input: String, inputType: String, inputKey: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val enumFeatures = p.features.filter(_.appliesToEnum)
    inputKey match {
      case "all" =>
        val i = projectile.getInput(input)
        val toSave = i.exportEnums.flatMap {
          case e if p.getEnumOpt(e.key).isDefined => None
          case e => Some(ProjectMember(input = input, inputType = e.inputType, key = e.key, features = enumFeatures))
        }
        val saved = projectile.saveProjectMembers(key, toSave)
        val redir = Redirect(controllers.project.routes.ProjectController.detail(key))
        Future.successful(redir.flashing("success" -> s"Saved ${saved.size} enums"))
      case _ =>
        val it = ProjectMember.InputType.withValue(inputType)
        val m = ProjectMember(input = input, inputType = it, key = inputKey, features = enumFeatures)
        projectile.saveProjectMembers(key, Seq(m))
        val redir = Redirect(controllers.project.routes.ProjectEnumController.detail(key, m.key))
        Future.successful(redir.flashing("success" -> s"Saved ${m.outputType} [${m.key}]"))
    }
  }

  def save(key: String, enumKey: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val m = p.getEnum(enumKey)

    val i = projectile.getInput(m.input)
    val e = i.exportEnum(m.key)

    val form = ControllerUtils.getForm(request.body)
    val newMember = m.copy(
      pkg = form("package").split('.').map(_.trim).filter(_.nonEmpty),
      features = form.getOrElse("features", "").split(',').map(_.trim).filter(_.nonEmpty).map(Feature.withValue).toSet,
      ignored = form.getOrElse("ignored", "").split(',').map(_.trim).filter(_.nonEmpty).toSet,
      overrides = Seq(
        form("propertyName") match {
          case x if x.nonEmpty && x != e.propertyName => Some(MemberOverride("propertyName", x))
          case _ => None
        },
        form("className") match {
          case x if x.nonEmpty && x != e.className => Some(MemberOverride("className", x))
          case _ => None
        }
      ).flatten
    )

    projectile.saveProjectMembers(key, Seq(newMember))
    val redir = Redirect(controllers.project.routes.ProjectEnumController.detail(key, enumKey))
    Future.successful(redir.flashing("success" -> s"Saved ${m.outputType} [$enumKey]"))
  }

  def remove(key: String, member: String) = Action.async { implicit request =>
    projectile.removeProjectMember(key, ProjectMember.OutputType.Enum, member)
    Future.successful(Redirect(controllers.project.routes.ProjectController.detail(key)).flashing("success" -> s"Removed enum [$member]"))
  }
}
