package controllers.project

import controllers.BaseController
import models.database.input.PostgresInput
import models.project.member.ProjectMember
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

    Future.successful(Ok(views.html.project.member.detailEnum(projectile, key, ee)))
  }

  def formNew(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val inputEnums = projectile.listInputs().map { input =>
      input.key -> (projectile.getInput(input.key) match {
        case pi: PostgresInput => pi.enums.map(e => (e.key, InputType.PostgresEnum.value, p.enums.exists(x => x.input == pi.key && x.inputKey == e.key)))
        case x => throw new IllegalStateException(s"Unhandled input [$x]")
      })
    }
    Future.successful(Ok(views.html.project.member.formNewEnum(projectile, key, inputEnums)))
  }

  def form(key: String, enum: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val m = p.getEnum(enum)
    Future.successful(Ok(views.html.project.member.formEnum(projectile, key, m)))
  }

  def add(key: String, input: String, inputType: String, inputKey: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val enumFeatures = p.features.filter(_.appliesToEnum)
    inputKey match {
      case "all" =>
        val i = projectile.getInput(input)
        val toSave = i.exportEnums.flatMap {
          case e if p.getEnumOpt(e.name).isDefined => None
          case e => Some(ProjectMember(input = input, inputType = e.inputType, inputKey = e.name, outputKey = e.name, features = enumFeatures))
        }
        val saved = projectile.saveProjectMembers(key, toSave)
        val redir = Redirect(controllers.project.routes.ProjectController.detail(key))
        Future.successful(redir.flashing("success" -> s"Saved ${saved.size} enums"))
      case _ =>
        val it = ProjectMember.InputType.withValue(inputType)
        val m = ProjectMember(input = input, inputType = it, inputKey = inputKey, outputKey = inputKey, features = enumFeatures)
        projectile.saveProjectMembers(key, Seq(m))
        val redir = Redirect(controllers.project.routes.ProjectEnumController.detail(key, m.outputKey))
        Future.successful(redir.flashing("success" -> s"Saved ${m.outputType} [${m.outputKey}]"))
    }
  }

  def save(key: String) = Action.async { implicit request =>
    val form = ControllerUtils.getForm(request.body)
    val m = ProjectMember(
      input = form("input"),
      inputType = ProjectMember.InputType.withValue(form("inputType")),
      inputKey = form("inputKey"),
      outputKey = form("outputKey"),
      ignored = form("ignored").split(',').map(_.trim).filter(_.nonEmpty),
      overrides = Nil
    )
    projectile.saveProjectMembers(key, Seq(m))
    val redir = Redirect(controllers.project.routes.ProjectEnumController.detail(key, m.outputKey))
    Future.successful(redir.flashing("success" -> s"Saved ${m.outputType} [${m.outputKey}]"))
  }

  def remove(key: String, member: String) = Action.async { implicit request =>
    projectile.removeProjectMember(key, ProjectMember.OutputType.Enum, member)
    Future.successful(Redirect(controllers.project.routes.ProjectController.detail(key)).flashing("success" -> s"Removed enum [$member]"))
  }
}
