package com.projectile.models.feature.core

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.core.db.{EnumFile, InjectIcons, ModelFile}
import com.projectile.models.feature.core.thrift.{IntEnumFile, ThriftServiceFile, StringEnumFile, StructModelFile}
import com.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature, ServiceFeature}
import com.projectile.models.project.member.{EnumMember, ModelMember, ServiceMember}

object CoreLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val enums = config.enums.filter(_.features(EnumFeature.Core)).flatMap { enum =>
      enum.inputType match {
        case EnumMember.InputType.PostgresEnum => Seq(EnumFile.export(config, enum).rendered)
        case EnumMember.InputType.ThriftIntEnum => Seq(IntEnumFile.export(config, enum).rendered)
        case EnumMember.InputType.ThriftStringEnum => Seq(StringEnumFile.export(config, enum).rendered)
        case EnumMember.InputType.GraphQLEnum => Nil
      }
    }
    val models = config.models.filter(_.features(ModelFeature.Core)).flatMap { model =>
      model.inputType match {
        case ModelMember.InputType.PostgresTable => Seq(ModelFile.export(config, model).rendered)
        case ModelMember.InputType.PostgresView => Seq(ModelFile.export(config, model).rendered)
        case ModelMember.InputType.ThriftStruct => Seq(StructModelFile.export(config, model).rendered)
        case m if m.isGraphQL => Nil
      }
    }
    val services = config.services.filter(_.features(ServiceFeature.Core)).flatMap { svc =>
      svc.inputType match {
        case ServiceMember.InputType.ThriftService => Seq(ThriftServiceFile.export(config, svc).rendered)
      }
    }
    debug(s"Exported [${enums.size}] enums, [${models.size}] models, and [${services.size}] services, creating [${models.size + enums.size}] files")
    enums ++ models ++ services
  }

  override val injections = Seq(InjectIcons)
}
