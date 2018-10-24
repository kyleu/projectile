package services.project

import models.export.config.ExportConfiguration
import models.output.OutputLog
import models.output.feature.{Feature, FeatureOutput}
import models.project.ProjectOutput
import services.ProjectileService

class ProjectExportService(val projectile: ProjectileService) {
  def getOutput(key: String, verbose: Boolean) = out(projectile.loadConfig(key), verbose)

  def exportProject(key: String, verbose: Boolean) = {
    val o = getOutput(key, verbose)

    /*
    val dir = "./tmp/boilerplay".toFile
    if (!dir.exists) {
      "git clone https://github.com/KyleU/boilerplay.git ./tmp/boilerplay".!!
      (dir / ".git").delete()
      (dir / "databaseflow.json").delete()
    }
    dir
    */

    o
  }

  private[this] def out(config: ExportConfiguration, verbose: Boolean) = {
    val startMs = System.currentTimeMillis

    val rootLogs = if (verbose) {
      Seq(OutputLog(s"Project Export (${config.project.features.size} features, ${config.enums.size} enums, ${config.models.size} models)", 0L))
    } else {
      Nil
    }

    val featureOutputs = Feature.values.flatMap {
      case feature if config.project.features(feature) => Seq(feature.export(config, verbose))
      case feature if verbose => Seq(FeatureOutput(feature, Nil, Seq(OutputLog(s"Skipping disabled feature [${feature.value}]", 0L)), 0L))
      case _ => Nil
    }

    ProjectOutput(project = config.project.toSummary, rootLogs = rootLogs, featureOutput = featureOutputs, duration = System.currentTimeMillis - startMs)
  }
}
