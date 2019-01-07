package com.kyleu.projectile.models.feature.core

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.core.db.{EnumFile, ModelFile}
import com.kyleu.projectile.models.feature.core.graphql.{GraphQLEnumFile, GraphQLFragmentFile, GraphQLInputFile, GraphQLOperationFile}
import com.kyleu.projectile.models.feature.core.thrift.{IntEnumFile, StringEnumFile, StructModelFile, ThriftServiceFile}
import com.kyleu.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature, ServiceFeature}
import com.kyleu.projectile.models.input.InputType

object CoreLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val enums = config.enums.filter(_.features(EnumFeature.Core)).flatMap { enum =>
      enum.inputType match {
        case InputType.Enum.PostgresEnum => Seq(EnumFile.export(config, enum).rendered)
        case InputType.Enum.ThriftIntEnum => Seq(IntEnumFile.export(config, enum).rendered)
        case InputType.Enum.ThriftStringEnum => Seq(StringEnumFile.export(config, enum).rendered)
        case InputType.Enum.GraphQLEnum => Seq(GraphQLEnumFile.export(config, enum).rendered)
      }
    }
    val models = config.models.filter(_.features(ModelFeature.Core)).flatMap { model =>
      model.inputType match {
        case InputType.Model.PostgresTable => Seq(ModelFile.export(config, model).rendered)
        case InputType.Model.PostgresView => Seq(ModelFile.export(config, model).rendered)
        case InputType.Model.ThriftStruct => Seq(StructModelFile.export(config, model).rendered)
        case InputType.Model.GraphQLFragment => Seq(GraphQLFragmentFile.export(config, model).rendered)
        case InputType.Model.GraphQLInput => Seq(GraphQLInputFile.export(config, model).rendered)
        case InputType.Model.GraphQLMutation => Seq(GraphQLOperationFile.export(config, model).rendered)
        case InputType.Model.GraphQLQuery => Seq(GraphQLOperationFile.export(config, model).rendered)
        case InputType.Model.GraphQLReference => Nil
      }
    }
    val services = config.services.filter(_.features(ServiceFeature.Core)).flatMap { svc =>
      svc.inputType match {
        case InputType.Service.ThriftService => Seq(ThriftServiceFile.export(config, svc).rendered)
      }
    }
    debug(s"Exported [${enums.size}] enums, [${models.size}] models, and [${services.size}] services, creating [${models.size + enums.size}] files")
    enums ++ models ++ services
  }
}
