package com.kyleu.projectile.models.feature.test

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.{FeatureLogic, ProjectFeature}

object TestLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    if (config.project.features(ProjectFeature.Tests) && config.project.features(ProjectFeature.Controller)) {
      Seq(ControllerTestFile.export(config).rendered)
    } else {
      Nil
    }
  }
}
