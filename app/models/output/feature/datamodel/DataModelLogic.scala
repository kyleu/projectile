package models.output.feature.datamodel

import models.export.config.ExportConfiguration
import models.output.feature.Feature

object DataModelLogic extends Feature.Logic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val results = config.models.filter(_.features(Feature.DataModel)).flatMap { model =>
      Seq(ResultFile.export(config, model).rendered)
    }
    debug(s"Exported [${results.size}] models")
    results
  }
}
