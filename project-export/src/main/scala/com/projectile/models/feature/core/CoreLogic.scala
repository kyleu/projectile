package com.projectile.models.feature.core

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.core.db.{EnumFile, InjectIcons, ModelFile}
import com.projectile.models.feature.core.graphql.{GraphQLEnumFile, GraphQLFragmentFile, GraphQLInputFile, GraphQLOperationFile}
import com.projectile.models.feature.core.thrift.{IntEnumFile, StringEnumFile, StructModelFile, ThriftServiceFile}
import com.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature, ServiceFeature}
import com.projectile.models.input.{EnumInputType, ModelInputType, ServiceInputType}

object CoreLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val enums = config.enums.filter(_.features(EnumFeature.Core)).flatMap { enum =>
      enum.inputType match {
        case EnumInputType.PostgresEnum => Seq(EnumFile.export(config, enum).rendered)
        case EnumInputType.ThriftIntEnum => Seq(IntEnumFile.export(config, enum).rendered)
        case EnumInputType.ThriftStringEnum => Seq(StringEnumFile.export(config, enum).rendered)
        case EnumInputType.GraphQLEnum => Seq(GraphQLEnumFile.export(config, enum).rendered)
      }
    }
    val models = config.models.filter(_.features(ModelFeature.Core)).flatMap { model =>
      model.inputType match {
        case ModelInputType.PostgresTable => Seq(ModelFile.export(config, model).rendered)
        case ModelInputType.PostgresView => Seq(ModelFile.export(config, model).rendered)
        case ModelInputType.ThriftStruct => Seq(StructModelFile.export(config, model).rendered)
        case ModelInputType.GraphQLFragment => Seq(GraphQLFragmentFile.export(config, model).rendered)
        case ModelInputType.GraphQLInput => Seq(GraphQLInputFile.export(config, model).rendered)
        case ModelInputType.GraphQLMutation => Seq(GraphQLOperationFile.export(config, model).rendered)
        case ModelInputType.GraphQLQuery => Seq(GraphQLOperationFile.export(config, model).rendered)
      }
    }
    val services = config.services.filter(_.features(ServiceFeature.Core)).flatMap { svc =>
      svc.inputType match {
        case ServiceInputType.ThriftService => Seq(ThriftServiceFile.export(config, svc).rendered)
      }
    }
    debug(s"Exported [${enums.size}] enums, [${models.size}] models, and [${services.size}] services, creating [${models.size + enums.size}] files")
    enums ++ models ++ services
  }

  override val injections = Seq(InjectIcons)
}
