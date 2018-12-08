package controllers.project

import com.projectile.models.database.input.PostgresInput
import com.projectile.models.feature.ModelFeature
import com.projectile.models.graphql.input.GraphQLInput
import com.projectile.models.input.InputType
import com.projectile.models.project.member.{MemberOverride, ModelMember}
import com.projectile.models.thrift.input.ThriftInput
import controllers.BaseController
import util.web.ControllerUtils

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectModelController @javax.inject.Inject() () extends BaseController {
  def detail(key: String, model: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val m = p.getModel(model)

    val i = projectile.getInput(m.input)
    val em = i.exportModel(model)
    val updated = em.apply(m)
    val fin = updated.copy(fields = em.fields.map(f => updated.getFieldOpt(f.key).getOrElse(f)))

    Future.successful(Ok(views.html.project.member.detailModel(projectile, key, p.toSummary, m, fin)))
  }

  def formNew(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val inputModels = projectile.listInputs().map { input =>
      input.key -> (projectile.getInput(input.key) match {
        case pi: PostgresInput =>
          val ts = pi.tables.map(m => (m.name, InputType.Model.PostgresTable.value, p.models.exists(x => x.input == pi.key && x.key == m.name)))
          val vs = pi.views.map(v => (v.name, InputType.Model.PostgresView.value, p.models.exists(x => x.input == pi.key && x.key == v.name)))
          ts ++ vs
        case ti: ThriftInput => ti.structs.map(s => (s.key, InputType.Model.ThriftStruct.value, p.models.exists(x => x.input == ti.key && x.key == s.key)))
        case gi: GraphQLInput => gi.exportModels.map(m => (m.key, m.inputType.value, p.models.exists(x => x.input == gi.key && x.key == m.key)))
        case x => throw new IllegalStateException(s"Unhandled input [$x]")
      })
    }
    Future.successful(Ok(views.html.project.member.formNewModel(projectile, key, inputModels)))
  }

  def add(key: String, input: String, inputType: String, inputKey: String) = Action.async { implicit request =>
    val i = projectile.getInput(input)
    val p = projectile.getProject(key)
    inputKey match {
      case "all" =>
        val toSave = i.exportModels.flatMap {
          case m if p.getModelOpt(m.key).isDefined => None
          case m => Some(ModelMember(input = input, pkg = m.pkg, key = m.key, features = p.modelFeatures.toSet))
        }
        val saved = projectile.saveModelMembers(key, toSave)
        val redir = Redirect(controllers.project.routes.ProjectController.detail(key))
        Future.successful(redir.flashing("success" -> s"Added ${saved.size} models"))
      case _ =>
        val orig = i.exportModel(inputKey)
        val m = ModelMember(input = input, pkg = orig.pkg, key = inputKey, features = p.modelFeatures.toSet)
        projectile.saveModelMembers(key, Seq(m))
        val redir = Redirect(controllers.project.routes.ProjectModelController.detail(key, m.key))
        Future.successful(redir.flashing("success" -> s"Added model [${m.key}]"))
    }
  }

  def save(key: String, modelKey: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val m = p.getModel(modelKey)

    val i = projectile.getInput(m.input)
    val model = i.exportModel(m.key)

    val form = ControllerUtils.getForm(request.body)

    val nameOverrides = Seq(
      form("propertyName") match {
        case x if x.nonEmpty && x != model.propertyName => Some(MemberOverride("propertyName", x))
        case _ => None
      },
      form("className") match {
        case x if x.nonEmpty && x != model.className => Some(MemberOverride("className", x))
        case _ => None
      },
      form("title") match {
        case x if x.nonEmpty && x != model.title => Some(MemberOverride("title", x))
        case _ => None
      },
      form("plural") match {
        case x if x.nonEmpty && x != model.plural => Some(MemberOverride("plural", x))
        case _ => None
      }
    ).flatten

    val fieldOverrides = model.fields.flatMap { f =>
      Seq(
        form.getOrElse(s"field-${f.key}-propertyName", "") match {
          case x if x.nonEmpty && x != f.propertyName => Some(MemberOverride(s"${f.key}.propertyName", x))
          case _ => None
        },
        form.getOrElse(s"field-${f.key}-title", "") match {
          case x if x.nonEmpty && x != f.title => Some(MemberOverride(s"${f.key}.title", x))
          case _ => None
        },
        form.getOrElse(s"field-${f.key}-search", "false") match {
          case x if x.toBoolean != f.inSearch => Some(MemberOverride(s"${f.key}.search", x))
          case _ => None
        }
      ).flatten
    }

    val foreignKeyOverrides = model.foreignKeys.flatMap { fk =>
      form.getOrElse(s"fk-${fk.name}-propertyName", "") match {
        case x if x.nonEmpty && x != fk.name => Some(MemberOverride(s"fk.${fk.name}.propertyName", x))
        case _ => None
      }
    }

    val referenceOverrides = model.references.flatMap { r =>
      form.getOrElse(s"reference-${r.name}-propertyName", "") match {
        case x if x.nonEmpty && x != r.name => Some(MemberOverride(s"reference.${r.name}.propertyName", x))
        case _ => None
      }
    }

    val newMember = m.copy(
      pkg = form("package").split('.').map(_.trim).filter(_.nonEmpty),
      features = form.getOrElse("features", "").split(',').map(_.trim).filter(_.nonEmpty).map(ModelFeature.withValue).toSet,
      ignored = form.getOrElse("ignored", "").split(',').map(_.trim).filter(_.nonEmpty).toSet,
      overrides = nameOverrides ++ fieldOverrides ++ foreignKeyOverrides ++ referenceOverrides
    )

    projectile.saveModelMembers(key, Seq(newMember))
    val redir = Redirect(controllers.project.routes.ProjectModelController.detail(key, modelKey))
    Future.successful(redir.flashing("success" -> s"Saved model [$modelKey]"))
  }

  def remove(key: String, member: String) = Action.async { implicit request =>
    projectile.removeModelMember(key, member)
    Future.successful(Redirect(controllers.project.routes.ProjectController.detail(key)).flashing("success" -> s"Removed model [$member]"))
  }
}
