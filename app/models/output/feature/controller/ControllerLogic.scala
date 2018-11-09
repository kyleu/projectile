package models.output.feature.controller

import better.files.File
import models.export.config.ExportConfiguration
import models.output.feature.{EnumFeature, ModelFeature, ProjectFeature}

object ControllerLogic extends ProjectFeature.Logic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val models = config.models.filter(_.features(ModelFeature.Controller)).flatMap { model =>
      Seq(ControllerFile.export(config, model).rendered)
    }

    val enums = config.enums.filter(_.features(EnumFeature.Controller)).flatMap { enum =>
      Seq(EnumControllerFile.export(config, enum).rendered)
    }

    debug(s"Exported [${models.size}] models and [${enums.size}] enums")
    models ++ enums
  }

  override def inject(config: ExportConfiguration, projectRoot: File, info: String => Unit, debug: String => Unit) = {
    InjectRoutes.inject(config, projectRoot, info, debug)
  }
}
