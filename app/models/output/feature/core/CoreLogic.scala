package models.output.feature.core

import better.files.File
import models.export.config.ExportConfiguration
import models.output.feature.{EnumFeature, ModelFeature, ProjectFeature}

object CoreLogic extends ProjectFeature.Logic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val enums = config.enums.filter(_.features(EnumFeature.Core)).flatMap { enum =>
      Seq(EnumFile.export(config, enum).rendered)
    }
    val models = config.models.filter(_.features(ModelFeature.Core)).flatMap { model =>
      Seq(ModelFile.export(config, model).rendered)
    }
    debug(s"Exported [${enums.size}] enums and [${models.size}] models, creating [${models.size + enums.size}] files")
    enums ++ models
  }

  override def inject(config: ExportConfiguration, projectRoot: File, info: String => Unit, debug: String => Unit) = {
    InjectIcons.inject(config, projectRoot, info, debug)
  }
}
