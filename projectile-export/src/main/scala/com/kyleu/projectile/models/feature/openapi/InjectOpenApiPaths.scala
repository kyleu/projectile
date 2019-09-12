package com.kyleu.projectile.models.feature.openapi

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature}
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.inject.{CommentProvider, TextSectionHelper}

object InjectOpenApiPaths extends FeatureLogic.Inject(path = OutputPath.ServerResource, filename = "paths.json") {
  override def applies(config: ExportConfiguration) = config.models.exists(m => m.features(ModelFeature.Controller) && m.inputType.isDatabase)
  override def dir(config: ExportConfiguration) = Seq("openapi", "paths")

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[(String, String)]], original: Seq[String]) = {
    val models = config.models.filter(_.features(ModelFeature.Controller)).filter(_.inputType.isDatabase).sortBy { x =>
      x.pkg.mkString("/") + "/" + x.propertyName
    }

    def enumPathsFor(lines: Seq[String]) = {
      val enums = config.enums.filter(_.features(EnumFeature.Controller)).filter(_.inputType.isDatabase)
      val newLines = enums.map { e =>
        s""""#include:${e.pkg.mkString("/")}/${e.propertyName}.json": "*","""
      }.sorted

      val params = TextSectionHelper.Params(commentProvider = CommentProvider.Json, key = "enum paths")
      TextSectionHelper.replaceBetween(filename = filename, original = lines, p = params, newLines = newLines, project = config.project.key)
    }

    def modelPathsFor(lines: Seq[String]) = {
      val newLines = models.map { m =>
        s""""#include:${m.pkg.mkString("/")}/${m.propertyName}.json": "*","""
      }.sorted

      val params = TextSectionHelper.Params(commentProvider = CommentProvider.Json, key = "schema paths")
      TextSectionHelper.replaceBetween(filename = filename, original = lines, p = params, newLines = newLines, project = config.project.key)
    }

    enumPathsFor(modelPathsFor(original))
  }
}
