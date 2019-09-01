package com.kyleu.projectile.services.project

import com.kyleu.projectile.models.feature.ProjectFeature
import com.kyleu.projectile.services.ProjectileService

trait ProjectFeatureHelper { this: ProjectileService =>
  def toggleFeature(key: String, feature: String) = {
    val p = getProjectSummary(key)
    val f = ProjectFeature.withValue(feature)
    val exists = p.features(f)
    saveProject(p.copy(features = if (exists) { p.features - f } else { p.features + f }))
    !exists
  }
}
