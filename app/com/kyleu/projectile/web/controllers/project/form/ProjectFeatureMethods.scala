package com.kyleu.projectile.web.controllers.project.form

import com.kyleu.projectile.models.feature.{EnumFeature, ModelFeature, ProjectFeature, ServiceFeature}
import com.kyleu.projectile.util.StringUtils

import scala.concurrent.Future

@javax.inject.Singleton
trait ProjectFeatureMethods { this: ProjectFormController =>
  def formFeatures(key: String) = Action.async { implicit request =>
    val p = projectile.getProject(key)
    val i = projectile.getInputSummary(p.input)
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.form.formProjectFeatures(projectile, i.template, p)))
  }

  def saveFeatures() = Action.async { implicit request =>
    val (summary, form) = getSummary(request)
    val features = StringUtils.toList(form.getOrElse("features", "")).map(ProjectFeature.withValue).toSet
    val newFeatures = features.diff(summary.features)
    val removedFeatures = summary.features.diff(features)

    val ef = EnumFeature.values.filter(f => newFeatures.exists(f.dependsOn))
    val mf = ModelFeature.values.filter(f => newFeatures.exists(f.dependsOn))
    val sf = ServiceFeature.values.filter(f => newFeatures.exists(f.dependsOn))

    val efr = EnumFeature.values.filter(f => removedFeatures.exists(f.dependsOn))
    val mfr = ModelFeature.values.filter(f => removedFeatures.exists(f.dependsOn))
    val sfr = ServiceFeature.values.filter(f => removedFeatures.exists(f.dependsOn))

    val efd = summary.defaultEnumFeatures ++ ef.map(_.value).toSet -- efr.map(_.value).toSet
    val mfd = summary.defaultModelFeatures ++ mf.map(_.value).toSet -- mfr.map(_.value).toSet
    val sfd = summary.defaultServiceFeatures ++ sf.map(_.value).toSet -- mfr.map(_.value).toSet

    val fin = summary.copy(features = features, defaultEnumFeatures = efd, defaultModelFeatures = mfd, defaultServiceFeatures = sfd)
    val project = projectile.saveProject(fin)

    if (ef.nonEmpty || efr.nonEmpty || mf.nonEmpty || mfr.nonEmpty || sf.nonEmpty || sfr.nonEmpty) {
      val p = projectile.getProject(summary.key)
      if (ef.nonEmpty || efr.nonEmpty) {
        projectile.saveEnumMembers(p.key, p.enums.map(e => e.copy(features = if (e.features.isEmpty) {
          Set.empty
        } else {
          e.features ++ ef -- efr
        })))
      }
      if (mf.nonEmpty || mfr.nonEmpty) {
        projectile.saveModelMembers(p.key, p.models.map(m => m.copy(features = if (m.features.isEmpty) {
          Set.empty
        } else {
          m.features ++ mf -- mfr
        })))
      }
      if (sf.nonEmpty || sfr.nonEmpty) {
        projectile.saveServiceMembers(p.key, p.services.map(s => s.copy(features = if (s.features.isEmpty) {
          Set.empty
        } else {
          s.features ++ sf -- sfr
        })))
      }
    }

    val call = com.kyleu.projectile.web.controllers.project.routes.ProjectController.detail(project.key)
    Future.successful(Redirect(call).flashing("success" -> s"Saved features for project [${project.key}]"))
  }
}
