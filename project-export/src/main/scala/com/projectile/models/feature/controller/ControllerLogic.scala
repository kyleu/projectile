package com.projectile.models.feature.controller

import com.projectile.models.export.ExportModel
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.controller.db.{ControllerFile, RoutesFiles}
import com.projectile.models.feature.controller.db.twirl._
import com.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature, ServiceFeature}
import com.projectile.models.project.member.{EnumMember, ModelMember, ServiceMember}

object ControllerLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val enums = config.enums.filter(_.features(EnumFeature.Controller)).map { enum =>
      EnumControllerFile.export(config, enum).rendered
    }

    val models = config.models.filter(_.features(ModelFeature.Controller)).flatMap { model =>
      model.inputType match {
        case ModelMember.InputType.PostgresTable => dbModelFiles(config, model)
        case ModelMember.InputType.PostgresView => dbModelFiles(config, model)
        case ModelMember.InputType.ThriftStruct => Nil
      }
    }

    val services = config.services.filter(_.features(ServiceFeature.Controller)).flatMap { service =>
      service.inputType match {
        case ServiceMember.InputType.ThriftService => Nil
      }
    }

    val routes = RoutesFiles.export(config).map(_.rendered)

    debug(s"Exported [${models.size}] models and [${enums.size}] enums")
    enums ++ models ++ services ++ routes
  }

  override val injections = Seq(InjectBindables, InjectRoutes, InjectSystemRoutes, InjectExploreHtml, InjectExploreMenu, InjectSearch)

  private[this] def dbModelFiles(config: ExportConfiguration, model: ExportModel) = Seq(
    ControllerFile.export(config, model).rendered,
    TwirlDataRowFile.export(config, model).rendered,
    TwirlListFile.export(config, model).rendered,
    TwirlViewFile.export(config, model).rendered,
    TwirlFormFile.export(config, model).rendered,
    TwirlSearchResultFile.export(config, model).rendered
  ) ++ TwirlRelationFiles.export(config, model).map(_.rendered)
}
