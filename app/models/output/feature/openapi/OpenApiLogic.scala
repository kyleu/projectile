package models.output.feature.openapi

import better.files.File
import models.export.config.ExportConfiguration
import models.output.feature.ProjectFeature

object OpenApiLogic extends ProjectFeature.Logic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val models = config.models.flatMap { model =>
      config.packages.find(_._2.contains(model)).map(_._4) match {
        case Some(solo) => Seq(ModelOpenApiSchemaFile.export(config, model).rendered, ModelOpenApiPathsFile.export(model, config.enums, solo = solo).rendered)
        case None => Nil
      }
    }

    val enums = config.enums.flatMap { enum =>
      Seq(EnumOpenApiSchemaFile.export(config, enum).rendered, EnumOpenApiPathsFile.export(config, enum).rendered)
    }

    debug(s"Exported [${models.size}] models and [${enums.size}] enums")
    models ++ enums
  }

  override def inject(config: ExportConfiguration, projectRoot: File, info: String => Unit, debug: String => Unit) = {
    InjectOpenApiPaths.inject(config, projectRoot, info, debug) ++ InjectOpenApiSchema.inject(config, projectRoot, info, debug)
  }
}
