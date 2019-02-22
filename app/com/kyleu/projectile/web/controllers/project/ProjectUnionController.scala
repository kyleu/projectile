package com.kyleu.projectile.web.controllers.project

import com.kyleu.projectile.models.project.member.{MemberOverride, UnionMember}
import com.kyleu.projectile.util.StringUtils
import com.kyleu.projectile.web.controllers.ProjectileController
import com.kyleu.projectile.web.util.ControllerUtils

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectUnionController @javax.inject.Inject() () extends ProjectileController {
  def detail(key: String, union: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val i = projectile.getInput(p.input)

    val s = p.getUnion(union)
    val em = i.exportUnion(union)
    val updated = em.apply(s)

    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.member.detailUnion(projectile, key, p.toSummary, s, updated)))
  }

  def formNew(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val i = projectile.getInput(p.input)

    val inputUnions = i.exportUnions.map(s => (s.key, p.unions.exists(x => x.key == s.key)))
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.member.formNewUnion(projectile, key, inputUnions)))
  }

  def add(key: String, unionKey: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val i = projectile.getInput(p.input)
    unionKey match {
      case "all" =>
        val toSave = i.exportUnions.flatMap {
          case m if p.getUnionOpt(m.key).isDefined => None
          case m => Some(UnionMember(pkg = m.pkg, key = m.key))
        }
        val saved = projectile.saveUnionMembers(key, toSave)
        val redir = Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectController.detail(key))
        Future.successful(redir.flashing("success" -> s"Added ${saved.size} unions"))
      case _ =>
        val orig = i.exportUnions.find(_.key == unionKey).getOrElse(throw new IllegalStateException(s"Cannot find union [$unionKey]"))
        val m = UnionMember(pkg = orig.pkg, key = unionKey)
        projectile.saveUnionMember(key, m)
        val redir = Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectUnionController.detail(key, m.key))
        Future.successful(redir.flashing("success" -> s"Added union [${m.key}]"))
    }
  }

  def save(key: String, unionKey: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val i = projectile.getInput(p.input)

    val m = p.getUnion(unionKey)
    val u = i.exportUnion(m.key)

    val form = ControllerUtils.getForm(request.body)
    val newMember = m.copy(
      pkg = StringUtils.toList(form("package"), '.'),
      ignored = StringUtils.toList(form.getOrElse("ignored", "")).toSet,
      overrides = Seq(
        form("propertyName") match {
          case x if x.nonEmpty && x != u.propertyName => Some(MemberOverride("propertyName", x))
          case _ => None
        },
        form("className") match {
          case x if x.nonEmpty && x != u.className => Some(MemberOverride("className", x))
          case _ => None
        }
      ).flatten
    )

    projectile.saveUnionMembers(key, Seq(newMember))
    val redir = Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectUnionController.detail(key, unionKey))
    Future.successful(redir.flashing("success" -> s"Saved union [$unionKey]"))
  }

  def remove(key: String, member: String) = Action.async { implicit request =>
    projectile.removeUnionMember(key, member)
    Future.successful(Redirect(com.kyleu.projectile.web.controllers.project.routes.ProjectController.detail(key)).flashing("success" -> s"Removed union [$member]"))
  }
}
