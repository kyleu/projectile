package com.kyleu.projectile.models.feature.controller

import com.kyleu.projectile.models.export.{ExportModel, ExportService}
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.controller.db.{ControllerFile, RoutesFiles}
import com.kyleu.projectile.models.feature.controller.db.twirl._
import com.kyleu.projectile.models.feature.controller.thrift.{ThriftControllerFile, ThriftRoutesFile, ThriftTwirlServiceFile}
import com.kyleu.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature, ServiceFeature}
import com.kyleu.projectile.models.input.InputType

object ControllerLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val enums = config.enums.filter(_.features(EnumFeature.Controller)).map { enum =>
      EnumControllerFile.export(config, enum).rendered
    }

    val models = config.models.filter(_.features(ModelFeature.Controller)).flatMap { model =>
      model.inputType match {
        case InputType.Model.PostgresTable => dbModelFiles(config, model)
        case InputType.Model.PostgresView => dbModelFiles(config, model)
        case InputType.Model.ThriftStruct => Nil
        case m if m.isGraphQL => Nil
      }
    }

    val services = config.services.filter(_.features(ServiceFeature.Controller)).flatMap { service =>
      service.inputType match {
        case InputType.Service.ThriftService => thriftServiceFiles(config, service)
      }
    }

    val routes = RoutesFiles.export(config).map(_.rendered)

    debug(s"Exported [${models.size}] models and [${enums.size}] enums")
    enums ++ models ++ services ++ routes
  }

  override val injections = Seq(InjectBindables, InjectExploreHtml, InjectExploreMenu, InjectIcons, InjectRoutes, InjectSearch, InjectSystemRoutes)

  private[this] def dbModelFiles(config: ExportConfiguration, model: ExportModel) = Seq(
    ControllerFile.export(config, model).rendered,
    TwirlDataRowFile.export(config, model).rendered,
    TwirlListFile.export(config, model).rendered,
    TwirlViewFile.export(config, model).rendered,
    TwirlFormFile.export(config, model).rendered,
    TwirlSearchResultFile.export(config, model).rendered
  ) ++ TwirlRelationFiles.export(config, model).map(_.rendered)

  private[this] def thriftServiceFiles(config: ExportConfiguration, service: ExportService) = Seq(
    ThriftControllerFile.export(config, service).rendered,
    ThriftRoutesFile.export(service).rendered,
    ThriftTwirlServiceFile.export(config, service).rendered
  )
}
