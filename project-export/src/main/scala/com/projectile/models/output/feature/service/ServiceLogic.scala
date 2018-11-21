package com.projectile.models.output.feature.service

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.feature.{FeatureLogic, ModelFeature}

object ServiceLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val svcModels = config.models.filter(_.features(ModelFeature.Service))

    val models = svcModels.flatMap { model =>
      Seq(QueriesFile.export(config, model).rendered, ServiceFile.export(config, model).rendered)
    }

    val registries = ServiceRegistryFiles.files(config, svcModels).map(_.rendered)

    debug(s"Exported [${models.size}] models and [${registries.size}] registries")
    models ++ registries
  }

  override val injections = Seq(InjectServiceRegistry)
}
