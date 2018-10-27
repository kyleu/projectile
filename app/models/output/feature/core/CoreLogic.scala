package models.output.feature.core

import models.export.config.ExportConfiguration
import models.output.feature.Feature

object CoreLogic extends Feature.Logic {

  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val enums = config.enums.filter(_.features(Feature.Core)).flatMap { enum =>
      Seq(EnumFile.export(config, enum).rendered)
    }
    val models = config.models.filter(_.features(Feature.Core)).flatMap { model =>
      Seq(ModelFile.export(config, model).rendered)
    }
    debug(s"Exported [${enums.size}] enums and [${models.size}] models, creating [${models.size + enums.size}] files")
    enums ++ models
  }
}
