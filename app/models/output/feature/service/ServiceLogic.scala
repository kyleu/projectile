package models.output.feature.service

import better.files.File
import models.export.config.ExportConfiguration
import models.output.feature.{ModelFeature, ProjectFeature}

object ServiceLogic extends ProjectFeature.Logic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val svcModels = config.models.filter(_.features(ModelFeature.Service))

    val models = svcModels.flatMap { model =>
      Seq(QueriesFile.export(config, model).rendered, ServiceFile.export(config, model).rendered)
    }

    val registries = ServiceRegistryFiles.files(config, svcModels).map(_.rendered)

    debug(s"Exported [${models.size}] models and [${registries.size}] registries")
    models ++ registries
  }

  override def inject(config: ExportConfiguration, projectRoot: File, info: String => Unit, debug: String => Unit) = {
    InjectServiceRegistry.inject(config, projectRoot, info, debug)
  }
}
