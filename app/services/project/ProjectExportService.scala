package services.project

import models.export.config.ExportConfiguration
import models.output.OutputLog
import models.output.feature.{Feature, FeatureOutput}
import models.project.ProjectOutput
import services.ProjectileService

class ProjectExportService(val projectile: ProjectileService) {
  def exportProject(key: String, verbose: Boolean) = {
    export(loadConfig(key), verbose)
  }

  private[this] def loadConfig(key: String) = {
    val p = projectile.getProject(key)
    val inputs = p.allMembers.map(_.input).distinct.map(projectile.getInput).map(i => i.key -> i).toMap

    // TODO apply overrides
    val exportEnums = p.enums.map(e => inputs(e.input).exportEnum(e.inputKey))
    val exportModels = p.models.map(e => inputs(e.input).exportModel(e.inputKey))

    ExportConfiguration(project = p, enums = exportEnums, models = exportModels)
  }

  private[this] def export(config: ExportConfiguration, verbose: Boolean) = {
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
