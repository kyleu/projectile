package com.projectile.models.feature.core

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.core.db.{EnumFile, InjectIcons, ModelFile}
import com.projectile.models.feature.core.thrift.{IntEnumFile, StringEnumFile}
import com.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature}
import com.projectile.models.project.member.{EnumMember, ModelMember}

object CoreLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val enums = config.enums.filter(_.features(EnumFeature.Core)).flatMap { enum =>
      enum.inputType match {
        case EnumMember.InputType.PostgresEnum => Seq(EnumFile.export(config, enum).rendered)
        case EnumMember.InputType.ThriftIntEnum => Seq(IntEnumFile.export(config, enum).rendered)
        case EnumMember.InputType.ThriftStringEnum => Seq(StringEnumFile.export(config, enum).rendered)
      }
    }
    val models = config.models.filter(_.features(ModelFeature.Core)).flatMap { model =>
      model.inputType match {
        case ModelMember.InputType.PostgresTable => Seq(ModelFile.export(config, model).rendered)
        case ModelMember.InputType.PostgresView => Seq(ModelFile.export(config, model).rendered)
        case ModelMember.InputType.ThriftStruct => Seq()
      }
    }
    debug(s"Exported [${enums.size}] enums and [${models.size}] models, creating [${models.size + enums.size}] files")
    enums ++ models
  }

  override val injections = Seq(InjectIcons)
}
