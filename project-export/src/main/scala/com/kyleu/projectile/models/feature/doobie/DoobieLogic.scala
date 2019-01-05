package com.kyleu.projectile.models.feature.doobie

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature}

object DoobieLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val models = config.models.filter(_.inputType.isDatabase).filter(_.features(ModelFeature.Doobie)).flatMap { model =>
      val testsOpt = if (model.features(ModelFeature.Tests)) { Seq(DoobieTestsFile.export(config, model).rendered) } else { Nil }
      DoobieFile.export(config, model).rendered +: testsOpt
    }

    val enums = config.enums.filter(_.inputType.isDatabase).filter(_.features(EnumFeature.Doobie)).flatMap { enum =>
      Seq(EnumDoobieFile.export(config, enum).rendered)
    }

    debug(s"Exported [${models.size}] models and [${enums.size}] enums")
    models ++ enums
  }
}
