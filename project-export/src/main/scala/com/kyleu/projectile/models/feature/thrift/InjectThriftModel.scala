package com.kyleu.projectile.models.feature.thrift

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.feature.{FeatureLogic, ModelFeature}
import com.kyleu.projectile.models.output.inject.{CommentProvider, TextSectionHelper}

object InjectThriftModel extends FeatureLogic.Inject(path = OutputPath.ThriftOutput, filename = "models.thrift") {
  override def dir(config: ExportConfiguration) = Nil

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: Seq[String]) = {
    val newLines = config.models.filter(_.features(ModelFeature.Thrift)).map { m =>
      s"""include "${("models" +: m.pkg).mkString("/")}/${m.className}.thrift""""
    }.sorted

    val params = TextSectionHelper.Params(commentProvider = CommentProvider.Thrift, key = "generated Thrift model includes")
    TextSectionHelper.replaceBetween(filename = filename, original = original, p = params, newLines = newLines, project = config.project.key)
  }
}
