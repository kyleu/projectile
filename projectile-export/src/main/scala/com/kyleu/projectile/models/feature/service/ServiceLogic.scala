package com.kyleu.projectile.models.feature.service

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.service.db.{QueriesFile, ServiceFile, ServiceTestFiles}
import com.kyleu.projectile.models.feature.{FeatureLogic, ModelFeature}

object ServiceLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val svcModels = config.models.filter(_.features(ModelFeature.Service))

    val modelFiles = svcModels.filter(_.inputType.isDatabase).flatMap { model =>
      Seq(QueriesFile.export(config, model).rendered, ServiceFile.export(config, model).rendered)
    }

    val testFiles = ServiceTestFiles.export(config).map(_.rendered)

    debug(s"Exported [${modelFiles.size}] models with [${testFiles.size}] test files")
    modelFiles ++ testFiles
  }

  override val injections = Seq(InjectIcons, InjectStartup)
}
