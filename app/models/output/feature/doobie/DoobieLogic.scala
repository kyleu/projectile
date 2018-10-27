package models.output.feature.doobie

import models.export.config.ExportConfiguration
import models.output.feature.Feature

object DoobieLogic extends Feature.Logic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val models = config.models.filter(_.features(Feature.Doobie)).flatMap { model =>
      Seq(DoobieFile.export(config, model).rendered, DoobieTestsFile.export(config, model).rendered)
    }

    val enums = config.enums.filter(_.features(Feature.Doobie)).flatMap { enum =>
      Seq(EnumDoobieFile.export(config, enum).rendered)
    }

    debug(s"Exported [${models.size}] models and [${enums.size}] enums")
    models ++ enums
  }
}
