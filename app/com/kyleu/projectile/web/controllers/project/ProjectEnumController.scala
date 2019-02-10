package com.kyleu.projectile.web.controllers.project

import com.kyleu.projectile.models.feature.EnumFeature
import com.kyleu.projectile.models.project.member
import com.kyleu.projectile.models.project.member.{EnumMember, MemberOverride}
import com.kyleu.projectile.util.StringUtils
import com.kyleu.projectile.web.controllers.ProjectileController
import com.kyleu.projectile.web.util.ControllerUtils

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectEnumController @javax.inject.Inject() () extends ProjectileController {
  def detail(key: String, enum: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val m = p.getEnum(enum)

    val i = projectile.getInput(p.input)
    val ee = i.exportEnum(enum)
    val fin = ee.apply(m).copy(values = ee.values)

    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.member.detailEnum(projectile, key, p.toSummary, m, fin)))
  }

  def formNew(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val i = projectile.getInput(p.input)

    val inputEnums = i.exportEnums.map(e => (e.key, p.enums.exists(x => x.key == e.key)))
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.member.formNewEnum(projectile, key, inputEnums)))
  }

  def add(key: String, enumKey: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val i = projectile.getInput(p.input)
    enumKey match {
      case "all" =>
        val toSave = i.exportEnums.flatMap {
          case e if p.getEnumOpt(e.key).isDefined => None
          case e => Some(member.EnumMember(pkg = e.pkg, key = e.key, features = p.defaultEnumFeatures.map(EnumFeature.withValue)))
        }
        val saved = projectile.saveEnumMembers(key, toSave)
        val redir = Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectController.detail(key))
        Future.successful(redir.flashing("success" -> s"Added ${saved.size} enums"))
      case _ =>
        val orig = i.exportEnum(enumKey)
        val m = EnumMember(pkg = orig.pkg, key = enumKey, features = p.defaultEnumFeatures.map(EnumFeature.withValue))
        projectile.saveEnumMember(key, m)
        val redir = Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectEnumController.detail(key, m.key))
        Future.successful(redir.flashing("success" -> s"Added enum [${m.key}]"))
    }
  }

  def save(key: String, enumKey: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val i = projectile.getInput(p.input)

    val m = p.getEnum(enumKey)
    val e = i.exportEnum(m.key)

    val form = ControllerUtils.getForm(request.body)
    val newMember = m.copy(
      pkg = StringUtils.toList(form("package"), '.'),
      features = StringUtils.toList(form.getOrElse("features", "")).map(EnumFeature.withValue).toSet,
      ignored = StringUtils.toList(form.getOrElse("ignored", "")).toSet,
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
    val redir = Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectEnumController.detail(key, enumKey))
    Future.successful(redir.flashing("success" -> s"Saved enum [$enumKey]"))
  }

  def remove(key: String, member: String) = Action.async { implicit request =>
    projectile.removeEnumMember(key, member)
    Future.successful(Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectController.detail(key)).flashing("success" -> s"Removed enum [$member]"))
  }

  def formFeatures(key: String) = Action.async { implicit request =>
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.form.formEnumFeatures(projectile, projectile.getProjectSummary(key))))
  }

  def saveFeatures(key: String) = Action.async { implicit request =>
    val form = ControllerUtils.getForm(request.body)
    val summary = projectile.getProjectSummary(key)
    val features = StringUtils.toList(form("features")).map(EnumFeature.withValue).map(_.value).toSet
    projectile.saveProject(summary.copy(defaultEnumFeatures = features))

    Future.successful(Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectController.detail(summary.key)).flashing(
      "success" -> s"Saved default enum features for project [${summary.key}]"
    ))
  }
}
