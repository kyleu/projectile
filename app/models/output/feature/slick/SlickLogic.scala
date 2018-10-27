package models.output.feature.slick

import models.export.config.ExportConfiguration
import models.output.feature.Feature

object SlickLogic extends Feature.Logic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val models = config.models.filter(_.features(Feature.Slick)).flatMap { model =>
      Seq(TableFile.export(config, model).rendered)
    }

    val enums = config.enums.filter(_.features(Feature.Slick)).flatMap { enum =>
      Seq(ColumnTypeFile.export(config, enum).rendered)
    }

    debug(s"Exported [${models.size}] models and [${enums.size}] enums")
    models ++ enums
  }
}
