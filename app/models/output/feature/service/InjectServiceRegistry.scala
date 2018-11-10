package models.output.feature.service

import models.export.config.ExportConfiguration
import models.output.feature.FeatureLogic
import models.output.{ExportHelper, OutputPath}

object InjectServiceRegistry extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "ServiceRegistry.scala") {
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "services"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: String) = {
    val startString = "    /* Start model service files */"
    val endString = "    /* End model service files */"
    val endIndex = original.indexOf(endString)

    val prefix = config.applicationPackage.map(_ + ".").mkString

    val newContent = {
      val withPackages = config.models.filter(_.pkg.nonEmpty).map(_.pkg.head).distinct.sorted.flatMap { p =>
        original.indexOf(s"val ${p}Services") match {
          case x if x > -1 && x > endIndex => None
          case _ => Some(s"""    val ${p}Services: ${prefix}services.$p.${ExportHelper.toClassName(p)}ServiceRegistry,""")
        }
      }.sorted.mkString("\n")

      val withoutPackages = config.models.filter(_.pkg.isEmpty).flatMap { m =>
        original.indexOf(s"val ${m.propertyName}Service") match {
          case x if x > -1 && x > endIndex => None
          case _ => Some(s"""    val ${m.propertyName}Service: ${prefix}services.${m.className}Service,""")
        }
      }.sorted.mkString("\n")

      val ws = if (withPackages.nonEmpty && withoutPackages.nonEmpty) { "\n\n" } else { "" }
      withPackages + ws + withoutPackages
    }

    ExportHelper.replaceBetween(filename = filename, original = original, start = startString, end = endString, newContent = newContent)
  }
}
