package models.output.feature.openapi

import better.files.File
import models.export.config.ExportConfiguration
import models.output.file.InjectResult
import models.output.{ExportHelper, OutputPath}

object InjectOpenApiPaths {
  def inject(config: ExportConfiguration, projectRoot: File, info: String => Unit, debug: String => Unit) = {
    val models = config.models.sortBy(x => x.pkg.mkString("/") + "/" + x.propertyName)

    def enumPathsFor(s: String) = {
      val newContent = if (config.enums.isEmpty) {
        ""
      } else {
        config.enums.map { e =>
          s"""  "#include:${e.pkg.mkString("/")}/${e.propertyName}.json": "*","""
        }.sorted.mkString("\n")
      }
      ExportHelper.replaceBetween(original = s, start = "  // Start enum paths", end = s"  // End enum paths", newContent = newContent)
    }

    def modelPathsFor(s: String) = {
      val newContent = models.map { m =>
        val comma = if (models.lastOption.contains(m)) { "" } else { "," }
        s"""  "#include:${m.pkg.mkString("/")}/${m.propertyName}.json": "*"$comma"""
      }.sorted.mkString("\n")
      ExportHelper.replaceBetween(original = s, start = "  // Start schema paths", end = s"  // End schema paths", newContent = newContent)
    }

    val dir = projectRoot / config.project.getPath(OutputPath.ServerResource)
    val f = dir / "openapi" / "components" / "schema" / "paths.json"

    if (f.exists) {
      val c = enumPathsFor(modelPathsFor(f.contentAsString))
      debug("Injected paths.json")
      Seq(InjectResult(path = OutputPath.ServerResource, dir = Seq("openapi", "components", "schema"), filename = "paths.json", content = c))
    } else {
      info(s"Cannot load file [${f.pathAsString}] for injection.")
      Nil
    }
  }
}
