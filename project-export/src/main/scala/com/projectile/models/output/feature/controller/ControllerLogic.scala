package com.projectile.models.output.feature.controller

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.feature.controller.twirl._
import com.projectile.models.output.feature.{EnumFeature, FeatureLogic, ModelFeature}

object ControllerLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val models = config.models.filter(_.features(ModelFeature.Controller)).flatMap { model =>
      Seq(
        ControllerFile.export(config, model).rendered,
        TwirlDataRowFile.export(config, model).rendered,
        TwirlListFile.export(config, model).rendered,
        TwirlViewFile.export(config, model).rendered,
        TwirlFormFile.export(config, model).rendered,
        TwirlSearchResultFile.export(config, model).rendered
      ) ++ TwirlRelationFiles.export(config, model).map(_.rendered) ++ RoutesFiles.export(config).map(_.rendered)
    }

    val enums = config.enums.filter(_.features(EnumFeature.Controller)).flatMap { enum =>
      Seq(EnumControllerFile.export(config, enum).rendered)
    }

    debug(s"Exported [${models.size}] models and [${enums.size}] enums")
    models ++ enums
  }

  override val injections = Seq(InjectBindables, InjectRoutes, InjectSystemRoutes, InjectExploreHtml, InjectExploreMenu, InjectSearch)
}
