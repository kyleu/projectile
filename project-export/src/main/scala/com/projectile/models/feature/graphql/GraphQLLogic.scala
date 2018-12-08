package com.projectile.models.feature.graphql

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.graphql.db.{EnumSchemaFile, GraphQLQueryFiles, SchemaFile}
import com.projectile.models.feature.graphql.thrift.{ThriftEnumSchemaFile, ThriftModelSchemaFile, ThriftServiceSchemaFile}
import com.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature, ServiceFeature}
import com.projectile.models.input.{EnumInputType, ModelInputType, ServiceInputType}

object GraphQLLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val enums = config.enums.filter(_.features(EnumFeature.GraphQL)).flatMap { enum =>
      enum.inputType match {
        case EnumInputType.PostgresEnum => Seq(EnumSchemaFile.export(config, enum).rendered)
        case EnumInputType.ThriftIntEnum => Seq(ThriftEnumSchemaFile.export(config, enum).rendered)
        case EnumInputType.ThriftStringEnum => Seq(ThriftEnumSchemaFile.export(config, enum).rendered)
        case EnumInputType.GraphQLEnum => Nil
      }
    }

    val models = config.models.filter(_.features(ModelFeature.GraphQL)).flatMap { model =>
      model.inputType match {
        case ModelInputType.PostgresTable => Seq(SchemaFile.export(config, model).rendered) ++ GraphQLQueryFiles.export(config, model).map(_.rendered)
        case ModelInputType.PostgresView => Seq(SchemaFile.export(config, model).rendered) ++ GraphQLQueryFiles.export(config, model).map(_.rendered)
        case ModelInputType.ThriftStruct => Seq(ThriftModelSchemaFile.export(config, model).rendered)
        case m if m.isGraphQL => Nil
      }
    }

    val services = config.services.filter(_.features(ServiceFeature.GraphQL)).flatMap { service =>
      service.inputType match {
        case ServiceInputType.ThriftService => Seq(ThriftServiceSchemaFile.export(config, service).rendered)
      }
    }

    debug(s"Exported [${enums.size}] enums, [${models.size}] models, and [${services.size}] services")
    models ++ enums ++ services
  }

  override val injections = Seq(InjectSchema)
}
