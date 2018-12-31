package com.kyleu.projectile.models.feature.service

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.{FeatureLogic, ModelFeature}

object ServiceLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val svcModels = config.models.filter(_.features(ModelFeature.Service))

    val models = svcModels.filter(_.inputType.isDatabase).flatMap { model =>
      Seq(QueriesFile.export(config, model).rendered, ServiceFile.export(config, model).rendered)
    }

    debug(s"Exported [${models.size}] models")
    models
  }
}
