package com.projectile.models.output.feature.thrift

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.{ExportHelper, OutputPath}
import com.projectile.models.output.feature.{FeatureLogic, ModelFeature}

object InjectThriftService extends FeatureLogic.Inject(path = OutputPath.ThriftOutput, filename = "services.thrift") {
  val startString = "/* Begin generated Thrift service includes */"
  val endString = "/* End generated Thrift service includes */"

  override def dir(config: ExportConfiguration) = Nil

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: String) = {
    val newContent = config.models.filter(_.features(ModelFeature.Thrift)).map { m =>
      s"""include "${("services" +: m.pkg).mkString("/")}/${m.className}Service.thrift""""
    }.sorted.mkString("\n")
    ExportHelper.replaceBetween(filename = filename, original = original, start = startString, end = endString, newContent = newContent)
  }
}
