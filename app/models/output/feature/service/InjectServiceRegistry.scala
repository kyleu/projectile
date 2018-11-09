package models.output.feature.service

import better.files.File
import models.export.config.ExportConfiguration
import models.output.file.InjectResult
import models.output.{ExportHelper, OutputPath}

object InjectServiceRegistry {
  def inject(config: ExportConfiguration, projectRoot: File, info: String => Unit, debug: String => Unit) = {
    def serviceFieldsFor(s: String) = {
      val startString = "    /* Start model service files */"
      val endString = "    /* End model service files */"
      val endIndex = s.indexOf(endString)

      val prefix = config.applicationPackage.map(_ + ".").mkString

      val newContent = {
        val withPackages = config.models.filter(_.pkg.nonEmpty).map(_.pkg.head).distinct.sorted.flatMap { p =>
          s.indexOf(s"val ${p}Services") match {
            case x if x > -1 && x > endIndex => None
            case _ => Some(s"""    val ${p}Services: ${prefix}services.$p.${ExportHelper.toClassName(p)}ServiceRegistry,""")
          }
        }.sorted.mkString("\n")

        val withoutPackages = config.models.filter(_.pkg.isEmpty).flatMap { m =>
          s.indexOf(s"val ${m.propertyName}Service") match {
            case x if x > -1 && x > endIndex => None
            case _ => Some(s"""    val ${m.propertyName}Service: ${prefix}services.${m.className}Service,""")
          }
        }.sorted.mkString("\n")

        val ws = if (withPackages.nonEmpty && withoutPackages.nonEmpty) { "\n\n" } else { "" }
        withPackages + ws + withoutPackages
      }
      ExportHelper.replaceBetween(original = s, start = startString, end = endString, newContent = newContent)
    }

    val dir = projectRoot / config.project.getPath(OutputPath.ServerSource)
    val f = dir / config.applicationPackage.mkString("/") / "services" / "ServiceRegistry.scala"

    if (f.exists) {
      val c = serviceFieldsFor(f.contentAsString)
      debug("Injected ServiceRegistry.scala")
      Seq(InjectResult(path = OutputPath.ServerSource, dir = config.applicationPackage :+ "services", filename = "ServiceRegistry.scala", content = c))
    } else {
      info(s"Cannot load file [${f.pathAsString}] for injection.")
      Nil
    }

  }
}
