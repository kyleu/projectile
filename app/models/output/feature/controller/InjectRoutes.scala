package models.output.feature.controller

import better.files.File
import models.export.config.ExportConfiguration
import models.output.file.InjectResult
import models.output.{ExportHelper, OutputPath}

object InjectRoutes {
  val startString = "# Start model route files"
  val endString = "# End model route files"

  def inject(config: ExportConfiguration, projectRoot: File, info: String => Unit, debug: String => Unit) = {
    val packages = config.packages.map(_._1)

    def routeFor(pkg: String) = {
      val detailUrl = s"/admin/$pkg"
      val detailWs = (0 until (39 - detailUrl.length)).map(_ => " ").mkString
      s"->          $detailUrl $detailWs $pkg.Routes"
    }

    def routesFor(s: String) = {
      val newContent = packages.map(routeFor).mkString("\n")
      ExportHelper.replaceBetween(
        original = s,
        start = startString,
        end = endString,
        newContent = newContent
      )
    }

    val dir = projectRoot / config.project.getPath(OutputPath.ServerResource)
    val f = dir / "routes"

    if (f.exists) {
      val c = routesFor(f.contentAsString)
      debug("Injected routes")
      Seq(InjectResult(path = OutputPath.ServerResource, dir = Nil, filename = "routes", content = c))
    } else {
      info(s"Cannot load file [${f.pathAsString}] for injection.")
      Nil
    }
  }
}
