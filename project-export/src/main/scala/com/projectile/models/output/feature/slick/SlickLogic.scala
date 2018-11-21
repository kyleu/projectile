package com.projectile.models.output.feature.slick

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.feature.{EnumFeature, FeatureLogic, ModelFeature}

object SlickLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val models = config.models.filter(_.features(ModelFeature.Slick)).flatMap { model =>
      Seq(TableFile.export(config, model).rendered)
    }

    val enums = config.enums.filter(_.features(EnumFeature.Slick)).flatMap { enum =>
      Seq(ColumnTypeFile.export(config, enum).rendered)
    }

    debug(s"Exported [${models.size}] models and [${enums.size}] enums")
    models ++ enums
  }
}
