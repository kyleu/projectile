package models.output.feature.openapi

import models.export.config.ExportConfiguration
import models.output.feature.Feature

object OpenApiLogic extends Feature.Logic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val models = config.models.filter(_.features(Feature.OpenAPI)).flatMap { model =>
      config.packages.find(_._2.contains(model)).map(_._4) match {
        case Some(solo) => Seq(ModelOpenApiSchemaFile.export(config, model).rendered, ModelOpenApiPathsFile.export(model, config.enums, solo = solo).rendered)
        case None => Nil
      }
    }

    val enums = config.enums.filter(_.features(Feature.OpenAPI)).flatMap { enum =>
      Seq(EnumOpenApiSchemaFile.export(enum).rendered, EnumOpenApiPathsFile.export(enum).rendered)
    }

    debug(s"Exported [${models.size}] models and [${enums.size}] enums")
    models ++ enums
  }
}
