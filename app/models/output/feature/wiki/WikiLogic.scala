package models.output.feature.wiki

import models.export.config.ExportConfiguration
import models.output.feature.Feature

object WikiLogic extends Feature.Logic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val listPages = WikiListFiles.export(config).map(_.rendered)

    val models = config.models.filter(_.features(Feature.Wiki)).flatMap { model =>
      Seq(WikiModelFile.export(config, model).rendered)
    }

    val enums = config.enums.filter(_.features(Feature.Wiki)).flatMap { e =>
      Seq(WikiEnumFile.export(config, e).rendered)
    }

    debug(s"Exported [${models.size}] models and [${enums.size}] enums")
    listPages ++ models ++ enums
  }
}
