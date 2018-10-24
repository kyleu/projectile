package models.output.feature.wiki

import models.export.config.ExportConfiguration
import models.output.feature.Feature

object WikiLogic extends Feature.Logic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val listPages = WikiListFiles.export(config).map(_.rendered)

    val modelResults = config.models.flatMap { model =>
      Seq(WikiModelFile.export(config, model).rendered)
    }

    val enumResults = config.enums.flatMap { e =>
      Seq(WikiEnumFile.export(config, e).rendered)
    }

    debug(s"Exported [${modelResults.size}] models and [${enumResults.size}] enums")
    listPages ++ modelResults ++ enumResults
  }
}
