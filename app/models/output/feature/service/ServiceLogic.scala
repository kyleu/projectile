package models.output.feature.service

import models.export.config.ExportConfiguration
import models.output.feature.Feature

object ServiceLogic extends Feature.Logic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val models = config.models.filter(_.features(Feature.Service)).flatMap { model =>
      Seq(QueriesFile.export(config, model).rendered)
    }

    val enums = config.enums.filter(_.features(Feature.Service)).flatMap { enum =>
      Nil
    }

    debug(s"Exported [${models.size}] models and [${enums.size}] enums")
    models ++ enums
  }
}
