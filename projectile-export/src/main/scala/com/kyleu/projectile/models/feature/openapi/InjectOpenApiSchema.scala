package com.kyleu.projectile.models.feature.openapi

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.{FeatureLogic, ModelFeature}
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.inject.{CommentProvider, TextSectionHelper}

object InjectOpenApiSchema extends FeatureLogic.Inject(path = OutputPath.ServerResource, filename = "schemas.json") {
  override def applies(config: ExportConfiguration) = config.models.exists(m => m.features(ModelFeature.Controller) && m.inputType.isDatabase)
  override def dir(config: ExportConfiguration) = Seq("openapi", "components", "schema")

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: Seq[String]) = {
    val models = config.models.sortBy(x => x.pkg.mkString("/") + "/" + x.propertyName)

    val newLines = models.filter(_.features(ModelFeature.Controller)).filter(_.inputType.isDatabase).map { m =>
      s""""#include:${m.pkg.mkString("/")}/${m.propertyName}.json": "*","""
    }.sorted

    val params = TextSectionHelper.Params(commentProvider = CommentProvider.Json, key = "model schemas")
    TextSectionHelper.replaceBetween(filename = filename, original = original, p = params, newLines = newLines, project = config.project.key)
  }
}
