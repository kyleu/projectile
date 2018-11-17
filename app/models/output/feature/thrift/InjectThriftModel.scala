package models.output.feature.thrift

import models.export.config.ExportConfiguration
import models.output.{ExportHelper, OutputPath}
import models.output.feature.{FeatureLogic, ModelFeature}

object InjectThriftModel extends FeatureLogic.Inject(path = OutputPath.ThriftOutput, filename = "models.thrift") {
  val startString = "/* Begin generated Thrift model includes */"
  val endString = "/* End generated Thrift model includes */"

  override def dir(config: ExportConfiguration) = Nil

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: String) = {
    val newContent = config.models.filter(_.features(ModelFeature.Thrift)).map { m =>
      s"""include "${("models" +: m.pkg).mkString("/")}/${m.className}.thrift""""
    }.sorted.mkString("\n")
    ExportHelper.replaceBetween(filename = filename, original = original, start = startString, end = endString, newContent = newContent)
  }
}
