package com.projectile.models.output.feature.openapi

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.feature.FeatureLogic

object OpenApiLogic extends FeatureLogic {
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

  override val injections = Seq(InjectOpenApiPaths, InjectOpenApiSchema)
}
