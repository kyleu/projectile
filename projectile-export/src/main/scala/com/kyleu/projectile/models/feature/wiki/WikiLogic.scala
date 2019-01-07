package com.kyleu.projectile.models.feature.wiki

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature}

object WikiLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val listPages = if (config.models.exists(_.inputType.isDatabase)) {
      WikiListFiles.export(config).map(_.rendered)
    } else {
      Nil
    }

    val models = config.models.filter(_.features(ModelFeature.Core)).filter(_.inputType.isDatabase).filter(_.inputType.isDatabase).flatMap { model =>
      Seq(WikiModelFile.export(config, model).rendered)
    }

    val enums = config.enums.filter(_.features(EnumFeature.Core)).filter(_.inputType.isDatabase).flatMap { e =>
      Seq(WikiEnumFile.export(config, e).rendered)
    }

    debug(s"Exported [${models.size}] models and [${enums.size}] enums")
    listPages ++ models ++ enums
  }
}
