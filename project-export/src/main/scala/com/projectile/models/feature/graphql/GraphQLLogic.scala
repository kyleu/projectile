package com.projectile.models.feature.graphql

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.graphql.db.{EnumSchemaFile, GraphQLQueryFiles, SchemaFile}
import com.projectile.models.feature.graphql.thrift.{ThriftEnumSchemaFile, ThriftModelSchemaFile}
import com.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature, ServiceFeature}
import com.projectile.models.project.member.{EnumMember, ModelMember, ServiceMember}

object GraphQLLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val enums = config.enums.filter(_.features(EnumFeature.GraphQL)).flatMap { enum =>
      enum.inputType match {
        case EnumMember.InputType.PostgresEnum => Seq(EnumSchemaFile.export(config, enum).rendered)
        case EnumMember.InputType.ThriftIntEnum => Seq(ThriftEnumSchemaFile.export(config, enum).rendered)
        case EnumMember.InputType.ThriftStringEnum => Seq(ThriftEnumSchemaFile.export(config, enum).rendered)
      }
    }

    val models = config.models.filter(_.features(ModelFeature.GraphQL)).flatMap { model =>
      model.inputType match {
        case ModelMember.InputType.PostgresTable => Seq(SchemaFile.export(config, model).rendered) ++ GraphQLQueryFiles.export(config, model).map(_.rendered)
        case ModelMember.InputType.PostgresView => Seq(SchemaFile.export(config, model).rendered) ++ GraphQLQueryFiles.export(config, model).map(_.rendered)
        case ModelMember.InputType.ThriftStruct => Seq(ThriftModelSchemaFile.export(config, model).rendered)
      }
    }

    val services = config.services.filter(_.features(ServiceFeature.GraphQL)).flatMap { model =>
      model.inputType match {
        case ServiceMember.InputType.ThriftService => Nil
      }
    }

    debug(s"Exported [${enums.size}] enums, [${models.size}] models, and [${services.size}] services")
    models ++ enums ++ services
  }

  override val injections = Seq(InjectSchema)
}
