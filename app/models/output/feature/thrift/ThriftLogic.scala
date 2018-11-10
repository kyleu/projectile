package models.output.feature.thrift

import models.export.config.ExportConfiguration
import models.output.feature.{FeatureLogic, ModelFeature}

object ThriftLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val models = config.models.filter(_.features(ModelFeature.Thrift)).flatMap { model =>
      Seq(ThriftModelFile.export(config, model).rendered, ThriftModelFile.export(config, model).rendered)
    }

    debug(s"Exported [${models.size}] models")
    models
  }
}
