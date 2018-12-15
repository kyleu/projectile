package com.kyleu.projectile.models.feature.openapi

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.FeatureLogic
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}

object InjectOpenApiPaths extends FeatureLogic.Inject(path = OutputPath.ServerResource, filename = "paths.json") {
  override def dir(config: ExportConfiguration) = Seq("openapi", "paths")

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: String) = {
    val models = config.models.sortBy(x => x.pkg.mkString("/") + "/" + x.propertyName)

    def enumPathsFor(s: String) = {
      val startString = "  // Start enum paths"
      val newContent = if (config.enums.isEmpty) {
        ""
      } else {
        config.enums.map { e =>
          s"""  "#include:${e.pkg.mkString("/")}/${e.propertyName}.json": "*","""
        }.sorted.mkString("\n")
      }
      ExportHelper.replaceBetween(filename = filename, original = original, start = startString, end = s"  // End enum paths", newContent = newContent)
    }

    def modelPathsFor(s: String) = {
      val startString = "  // Start schema paths"
      val newContent = models.map { m =>
        val comma = if (models.lastOption.contains(m)) { "" } else { "," }
        s"""  "#include:${m.pkg.mkString("/")}/${m.propertyName}.json": "*"$comma"""
      }.sorted.mkString("\n")
      ExportHelper.replaceBetween(filename = filename, original = original, start = startString, end = s"  // End schema paths", newContent = newContent)
    }

    enumPathsFor(modelPathsFor(original))
  }
}
