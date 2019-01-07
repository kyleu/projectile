package com.kyleu.projectile.models.feature.graphql

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.graphql.db.{GraphQLQueryFiles, SchemaFile}
import com.kyleu.projectile.models.feature.graphql.thrift.{ThriftModelSchemaFile, ThriftServiceSchemaFile}
import com.kyleu.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature, ServiceFeature}
import com.kyleu.projectile.models.input.InputType

object GraphQLLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val enums = config.enums.filter(_.features(EnumFeature.GraphQL)).flatMap { enum =>
      enum.inputType match {
        case InputType.Enum.GraphQLEnum => Nil
        case _ => Seq(EnumSchemaFile.export(config, enum).rendered)
      }
    }

    val models = config.models.filter(_.features(ModelFeature.GraphQL)).flatMap { model =>
      model.inputType match {
        case InputType.Model.PostgresTable => Seq(SchemaFile.export(config, model).rendered) ++ GraphQLQueryFiles.export(config, model).map(_.rendered)
        case InputType.Model.PostgresView => Seq(SchemaFile.export(config, model).rendered) ++ GraphQLQueryFiles.export(config, model).map(_.rendered)
        case InputType.Model.ThriftStruct => Seq(ThriftModelSchemaFile.export(config, model).rendered)
        case m if m.isGraphQL => Nil
      }
    }

    val services = config.services.filter(_.features(ServiceFeature.GraphQL)).flatMap { service =>
      service.inputType match {
        case InputType.Service.ThriftService => Seq(ThriftServiceSchemaFile.export(config, service).rendered)
      }
    }

    debug(s"Exported [${enums.size}] enums, [${models.size}] models, and [${services.size}] services")
    models ++ enums ++ services
  }

  override val injections = Seq(InjectSchema)
}
