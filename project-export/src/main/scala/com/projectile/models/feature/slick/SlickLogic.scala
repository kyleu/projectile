package com.projectile.models.feature.slick

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature}

object SlickLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val models = config.models.filter(_.inputType.isDatabase).filter(_.features(ModelFeature.Slick)).flatMap { model =>
      Seq(TableFile.export(config, model).rendered)
    }

    val enums = config.enums.filter(_.inputType.isDatabase).filter(_.features(EnumFeature.Slick)).flatMap { enum =>
      Seq(ColumnTypeFile.export(config, enum).rendered)
    }

    val allTables = if (models.isEmpty && enums.isEmpty) {
      Nil
    } else {
      Seq(AllTablesFile.export(config).rendered)
    }

    debug(s"Exported [${models.size}] models and [${enums.size}] enums")
    models ++ enums ++ allTables
  }
}
