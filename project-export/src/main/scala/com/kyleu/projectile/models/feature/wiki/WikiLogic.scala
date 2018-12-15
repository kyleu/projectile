package com.kyleu.projectile.models.feature.wiki

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.FeatureLogic

object WikiLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val listPages = if (config.models.exists(_.inputType.isDatabase)) {
      WikiListFiles.export(config).map(_.rendered)
    } else {
      Nil
    }

    val models = config.models.filter(_.inputType.isDatabase).flatMap { model =>
      Seq(WikiModelFile.export(config, model).rendered)
    }

    val enums = config.enums.flatMap { e =>
      Seq(WikiEnumFile.export(config, e).rendered)
    }

    debug(s"Exported [${models.size}] models and [${enums.size}] enums")
    listPages ++ models ++ enums
  }
}
