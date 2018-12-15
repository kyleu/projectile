package com.kyleu.projectile.models.feature.openapi

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.FeatureLogic
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}

object InjectOpenApiSchema extends FeatureLogic.Inject(path = OutputPath.ServerResource, filename = "schemas.json") {
  val startString = "  // Start model schemas"
  val endString = s"  // End model schemas"

  override def dir(config: ExportConfiguration) = Seq("openapi", "components", "schema")

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: String) = {
    val models = config.models.sortBy(x => x.pkg.mkString("/") + "/" + x.propertyName)

    val newContent = models.map { m =>
      val comma = if (models.lastOption.contains(m)) { "" } else { "," }
      s"""  "#include:${m.pkg.mkString("/")}/${m.propertyName}.json": "*"$comma"""
    }.sorted.mkString("\n")
    ExportHelper.replaceBetween(filename = filename, original = original, start = startString, end = endString, newContent = newContent)
  }
}
