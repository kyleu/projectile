package com.kyleu.projectile.models.feature.thrift

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.{FeatureLogic, ModelFeature}

object ThriftLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val models = config.models.filter(_.inputType.isDatabase).filter(_.features(ModelFeature.Thrift)).flatMap { model =>
      Seq(ThriftModelFile.export(config, model).rendered, ThriftServiceFile.export(config, model).rendered)
    }

    debug(s"Exported [${models.size}] models")
    models
  }

  override def injections = Seq(InjectThriftModel, InjectThriftService)
}
