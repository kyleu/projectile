package com.projectile.models.feature.graphql

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature}

object GraphQLLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val models = config.models.filter(_.features(ModelFeature.GraphQL)).flatMap { model =>
      Seq(SchemaFile.export(config, model).rendered) ++ GraphQLQueryFiles.export(config, model).map(_.rendered)
    }

    val enums = config.enums.filter(_.features(EnumFeature.GraphQL)).flatMap { enum =>
      Seq(EnumSchemaFile.export(config, enum).rendered)
    }

    debug(s"Exported [${models.size}] models and [${enums.size}] enums")
    models ++ enums
  }

  override val injections = Seq(InjectSchema)
}
