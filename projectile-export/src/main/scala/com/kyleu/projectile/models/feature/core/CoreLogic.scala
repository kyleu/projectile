package com.kyleu.projectile.models.feature.core

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.core.db.{EnumFile, ModelFile}
import com.kyleu.projectile.models.feature.core.graphql.{GraphQLEnumFile, GraphQLFragmentFile, GraphQLInputFile, GraphQLOperationFile}
import com.kyleu.projectile.models.feature.core.thrift._
import com.kyleu.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature, ServiceFeature}
import com.kyleu.projectile.models.input.InputType._

object CoreLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val enums = config.enums.filter(_.features(EnumFeature.Core)).flatMap { enum =>
      enum.inputType match {
        case Enum.PostgresEnum => Seq(EnumFile.export(config, enum).rendered)
        case Enum.ThriftIntEnum => Seq(IntEnumFile.export(config, enum).rendered)
        case Enum.ThriftStringEnum => Seq(StringEnumFile.export(config, enum).rendered)
        case Enum.GraphQLEnum => Seq(GraphQLEnumFile.export(config, enum).rendered)
        case Enum.TypeScriptEnum => Nil
      }
    }
    val models = config.models.filter(_.features(ModelFeature.Core)).flatMap { model =>
      model.inputType match {
        case Model.PostgresTable | Model.PostgresView => Seq(ModelFile.export(config, model).rendered)
        case Model.ThriftStruct => Seq(StructModelFile.export(config, model).rendered)
        case Model.GraphQLFragment => Seq(GraphQLFragmentFile.export(config, model).rendered)
        case Model.GraphQLInput => Seq(GraphQLInputFile.export(config, model).rendered)
        case Model.GraphQLMutation => Seq(GraphQLOperationFile.export(config, model).rendered)
        case Model.GraphQLQuery => Seq(GraphQLOperationFile.export(config, model).rendered)
        case Model.GraphQLReference => Nil
      }
    }
    val unions = config.unions.flatMap { union =>
      union.inputType match {
        case Union.ThriftUnion => Seq(ThriftUnionFile.export(config, union).rendered)
        case Union.GraphQLUnion => Nil
      }
    }
    val services = config.services.filter(_.features(ServiceFeature.Core)).flatMap { svc =>
      svc.inputType match {
        case Service.ThriftService => Seq(ThriftServiceFile.export(config, svc).rendered)
      }
    }
    val additional = config.additional.map(_.rendered)
    val fc = enums.size + models.size + unions.size + services.size + additional.size
    debug(s"Exported [${enums.size}] enums, [${models.size}] models, [${unions.size}] unions, and [${services.size}] services, creating [$fc] files")
    additional ++ enums ++ models ++ unions ++ services
  }
}
