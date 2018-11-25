package com.projectile.models.feature.datamodel

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.{FeatureLogic, ModelFeature}
import com.projectile.models.project.member.ModelMember

object DataModelLogic extends FeatureLogic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    val results = config.models.filter(_.features(ModelFeature.DataModel)).flatMap { model =>
      model.inputType match {
        case ModelMember.InputType.PostgresTable => Seq(ResultFile.export(config, model).rendered)
        case ModelMember.InputType.PostgresView => Seq(ResultFile.export(config, model).rendered)
        case ModelMember.InputType.ThriftStruct => Seq()
      }

    }
    debug(s"Exported [${results.size}] models")
    results
  }
}
