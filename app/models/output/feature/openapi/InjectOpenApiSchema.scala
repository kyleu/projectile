package models.output.feature.openapi

import better.files.File
import models.export.config.ExportConfiguration
import models.output.file.InjectResult
import models.output.{ExportHelper, OutputPath}

object InjectOpenApiSchema {
  def inject(config: ExportConfiguration, projectRoot: File, info: String => Unit, debug: String => Unit) = {
    val models = config.models.sortBy(x => x.pkg.mkString("/") + "/" + x.propertyName)

    def modelSchemasFor(s: String) = {
      val newContent = models.map { m =>
        val comma = if (models.lastOption.contains(m)) { "" } else { "," }
        s"""  "#include:${m.pkg.mkString("/")}/${m.propertyName}.json": "*"$comma"""
      }.sorted.mkString("\n")
      ExportHelper.replaceBetween(original = s, start = "  // Start model schemas", end = s"  // End model schemas", newContent = newContent)
    }

    val dir = projectRoot / config.project.getPath(OutputPath.ServerResource)
    val f = dir / "openapi" / "components" / "schema" / "schemas.json"

    if (f.exists) {
      val c = modelSchemasFor(f.contentAsString)
      debug("Injected schemas.json")
      Seq(InjectResult(path = OutputPath.ServerResource, dir = Seq("openapi", "components", "schema"), filename = "schemas.json", content = c))
    } else {
      info(s"Cannot load file [${f.pathAsString}] for injection.")
      Nil
    }
  }
}
