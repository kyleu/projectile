package com.projectile.models.feature.core

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature}

object CoreLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val enums = config.enums.filter(_.features(EnumFeature.Core)).flatMap { enum =>
      Seq(StringEnumFile.export(config, enum).rendered)
    }
    val models = config.models.filter(_.features(ModelFeature.Core)).flatMap { model =>
      Seq(ModelFile.export(config, model).rendered)
    }
    debug(s"Exported [${enums.size}] enums and [${models.size}] models, creating [${models.size + enums.size}] files")
    enums ++ models
  }

  override val injections = Seq(InjectIcons)
}
