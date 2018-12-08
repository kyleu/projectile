package com.projectile.models.feature.datamodel

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.{FeatureLogic, ModelFeature}

object DataModelLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val results = config.models.filter(_.inputType.isDatabase).filter(_.features(ModelFeature.DataModel)).map { model =>
      ResultFile.export(config, model).rendered
    }
    debug(s"Exported [${results.size}] models")
    results
  }
}
