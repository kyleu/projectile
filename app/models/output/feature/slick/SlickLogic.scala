package models.output.feature.slick

import models.export.config.ExportConfiguration
import models.output.feature.Feature

object SlickLogic extends Feature.Logic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val tableResults = config.models.flatMap { model =>
      Seq(TableFile.export(config, model).rendered)
    }

    val enumResults = config.enums.flatMap { enum =>
      Seq(ColumnTypeFile.export(config, enum).rendered)
    }

    debug(s"Exported [${tableResults.size}] models and [${enumResults.size}] enums")
    tableResults ++ enumResults
  }
}
