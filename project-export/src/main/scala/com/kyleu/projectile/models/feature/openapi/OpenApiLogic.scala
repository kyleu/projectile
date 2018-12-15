package com.kyleu.projectile.models.feature.openapi

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature}

object OpenApiLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val models = config.models.filter(_.inputType.isDatabase).filter(_.features(ModelFeature.Controller)).filter(_.inputType.isDatabase).flatMap { model =>
      val p = model.pkg.headOption
      val solo = (config.models.count(_.pkg.headOption == p) + config.enums.count(_.pkg.headOption == p)) == 1
      Seq(ModelOpenApiSchemaFile.export(config, model).rendered, ModelOpenApiPathsFile.export(model, config.enums, solo = solo).rendered)
    }

    val enums = config.enums.filter(_.inputType.isDatabase).filter(_.features(EnumFeature.Controller)).flatMap { enum =>
      Seq(EnumOpenApiSchemaFile.export(config, enum).rendered, EnumOpenApiPathsFile.export(config, enum).rendered)
    }

    debug(s"Exported [${models.size}] models and [${enums.size}] enums")
    models ++ enums
  }

  override val injections = Seq(InjectOpenApiPaths, InjectOpenApiSchema)
}
