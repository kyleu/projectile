package com.projectile.models.output.feature.wiki

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.feature.FeatureLogic

object WikiLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val listPages = WikiListFiles.export(config).map(_.rendered)

    val models = config.models.flatMap { model =>
      Seq(WikiModelFile.export(config, model).rendered)
    }

    val enums = config.enums.flatMap { e =>
      Seq(WikiEnumFile.export(config, e).rendered)
    }

    debug(s"Exported [${models.size}] models and [${enums.size}] enums")
    listPages ++ models ++ enums
  }
}
