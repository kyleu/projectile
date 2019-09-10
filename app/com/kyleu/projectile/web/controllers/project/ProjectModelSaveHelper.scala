package com.kyleu.projectile.web.controllers.project

import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.project.member.MemberOverride
import com.kyleu.projectile.services.ProjectileService
import com.kyleu.projectile.util.StringUtils

object ProjectModelSaveHelper {
  def save(projectile: ProjectileService, key: String, modelKey: String, form: Map[String, String]) = {
    val p = projectile.getProject(key)
    val m = p.getModel(modelKey)

    val i = p.getInput
    val model = i.model(m.key)

    def or(k: String) = form(k) match {
      case x if x.nonEmpty && x != model.propertyName => Some(MemberOverride(k, x))
      case _ => None
    }
    val nameOverrides = Seq(or("propertyName"), or("className"), or("title"), or("plural")).flatten

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
        form.getOrElse(s"field-${f.key}-type", "") match {
          case x if x.nonEmpty && x != f.t.value => Some(MemberOverride(s"${f.key}.type", x))
          case _ => None
        },
        form.getOrElse(s"field-${f.key}-summary", "false") match {
          case x if x.toBoolean != f.inSummary => Some(MemberOverride(s"${f.key}.summary", x))
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

    val sortOverrides = form.getOrElse(s"defaultOrder", "") match {
      case x if x.nonEmpty => Some(MemberOverride("defaultOrder", x))
      case _ => None
    }

    val newMember = m.copy(
      pkg = StringUtils.toList(form("package"), '.'),
      features = StringUtils.toList(form.getOrElse("features", "")).map(ModelFeature.withValue).toSet,
      ignored = StringUtils.toList(form.getOrElse("ignored", "")).toSet,
      overrides = nameOverrides ++ fieldOverrides ++ foreignKeyOverrides ++ referenceOverrides ++ sortOverrides
    )

    projectile.saveModelMembers(key, Seq(newMember))
  }
}
