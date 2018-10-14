package services.project

import models.export.config.ExportConfiguration
import models.project.ProjectOutput
import models.project.feature.{FeatureOutput, ProjectFeature}
import services.ProjectileService

class ProjectExportService(val projectile: ProjectileService) {
  def exportProject(key: String, verbose: Boolean) = export(loadConfig(key), verbose)

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
      Seq(s"Project Export (${config.project.features.size} features, ${config.enums.size} enums, ${config.models.size} models)")
    } else {
      Nil
    }

    val featureOutputs = ProjectFeature.values.flatMap {
      case feature if config.project.features(feature) => Seq(exportFeature(config, feature, verbose))
      case feature if verbose => Seq(FeatureOutput(feature, Nil, Seq(s"Skipping disabled feature [${feature.value}]"), 0L))
      case _ => Nil
    }

    ProjectOutput(project = config.project.key, rootLogs = rootLogs, featureOutput = featureOutputs, duration = System.currentTimeMillis - startMs)
  }

  private[this] def exportFeature(config: ExportConfiguration, feature: ProjectFeature, verbose: Boolean) = {
    val startMs = System.currentTimeMillis
    val logs = Seq(s"${feature.title}...")
    FeatureOutput(feature = feature, files = Nil, logs = logs, duration = System.currentTimeMillis - startMs)
  }
}
