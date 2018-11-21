package com.projectile.services.project

import better.files.File
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.{OutputLog, OutputPath}
import com.projectile.models.output.feature.{FeatureOutput, ProjectFeature}
import com.projectile.models.project.ProjectOutput
import com.projectile.services.ProjectileService

class ProjectExportService(val projectile: ProjectileService) {
  def getOutput(projectRoot: File, key: String, verbose: Boolean) = out(projectRoot, projectile.loadConfig(key), verbose)

  def exportProject(projectRoot: File, key: String, verbose: Boolean) = {
    val o = getOutput(projectRoot, key, verbose)

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

  private[this] def out(projectRoot: File, config: ExportConfiguration, verbose: Boolean) = {
    val startMs = System.currentTimeMillis

    val rootLogs = if (verbose) {
      Seq(OutputLog(s"Project Export (${config.project.features.size} features, ${config.enums.size} enums, ${config.models.size} models)", 0L))
    } else {
      Nil
    }

    val featureOutputs = ProjectFeature.values.flatMap {
      case feature if config.project.features(feature) => Seq(feature.export(projectRoot, config, verbose))
      case feature if verbose => Seq(FeatureOutput(feature, Nil, Nil, Seq(OutputLog(s"Skipping disabled feature [${feature.value}]", 0L)), 0L))
      case _ => Nil
    }

    ProjectOutput(project = config.project.toSummary, rootLogs = rootLogs, featureOutput = featureOutputs, duration = System.currentTimeMillis - startMs)
  }
}
