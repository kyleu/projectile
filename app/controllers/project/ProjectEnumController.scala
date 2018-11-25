package controllers.project

import com.projectile.models.database.input.PostgresInput
import com.projectile.models.output.feature.EnumFeature
import com.projectile.models.project.member
import com.projectile.models.project.member.EnumMember.InputType
import com.projectile.models.project.member.{EnumMember, MemberOverride}
import com.projectile.models.thrift.input.ThriftInput
import controllers.BaseController
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
        case ti: ThriftInput =>
          val i = ti.intEnums.map(e => (e.key, InputType.ThriftIntEnum.value, p.enums.exists(x => x.input == ti.key && x.key == e.key)))
          val s = ti.stringEnums.map(e => (e.key, InputType.ThriftStringEnum.value, p.enums.exists(x => x.input == ti.key && x.key == e.key)))
          i ++ s
        case x => throw new IllegalStateException(s"Unhandled input [$x]")
      })
    }
    Future.successful(Ok(views.html.project.member.formNewEnum(projectile, key, inputEnums)))
  }

  def add(key: String, input: String, inputType: String, inputKey: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    inputKey match {
      case "all" =>
        val i = projectile.getInput(input)
        val toSave = i.exportEnums.flatMap {
          case e if p.getEnumOpt(e.key).isDefined => None
          case e => Some(member.EnumMember(input = input, inputType = e.inputType, key = e.key, features = p.enumFeatures.toSet))
        }
        val saved = projectile.saveEnumMembers(key, toSave)
        val redir = Redirect(controllers.project.routes.ProjectController.detail(key))
        Future.successful(redir.flashing("success" -> s"Added ${saved.size} enums"))
      case _ =>
        val it = EnumMember.InputType.withValue(inputType)
        val m = EnumMember(input = input, inputType = it, key = inputKey, features = p.enumFeatures.toSet)
        projectile.saveEnumMembers(key, Seq(m))
        val redir = Redirect(controllers.project.routes.ProjectEnumController.detail(key, m.key))
        Future.successful(redir.flashing("success" -> s"Added enum [${m.key}]"))
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
      features = form.getOrElse("features", "").split(',').map(_.trim).filter(_.nonEmpty).map(EnumFeature.withValue).toSet,
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

    projectile.saveEnumMembers(key, Seq(newMember))
    val redir = Redirect(controllers.project.routes.ProjectEnumController.detail(key, enumKey))
    Future.successful(redir.flashing("success" -> s"Saved enum [$enumKey]"))
  }

  def remove(key: String, member: String) = Action.async { implicit request =>
    projectile.removeEnumMember(key, member)
    Future.successful(Redirect(controllers.project.routes.ProjectController.detail(key)).flashing("success" -> s"Removed enum [$member]"))
  }
}
