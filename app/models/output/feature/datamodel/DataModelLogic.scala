package models.output.feature.datamodel

import models.export.config.ExportConfiguration
import models.output.feature.{FeatureLogic, ModelFeature}

object DataModelLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val results = config.models.filter(_.features(ModelFeature.DataModel)).flatMap { model =>
      Seq(ResultFile.export(config, model).rendered)
    }
    debug(s"Exported [${results.size}] models")
    results
  }
}
