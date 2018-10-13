package services.project

import models.export.config.ExportConfiguration
import services.ProjectileService

class ProjectExportService(val projectile: ProjectileService) {
  def exportProject(key: String) = {
    val config = loadConfig(key)
    export(config)
    s"TODO: Project Export (${config.enums.size} enums, ${config.models.size} models)"
  }

  private[this] def loadConfig(key: String) = {
    val p = projectile.getProject(key)
    val inputs = p.allMembers.map(_.input).distinct.map(projectile.getInput).map(i => i.key -> i).toMap

    // TODO apply overrides
    val exportEnums = p.enums.map(e => inputs(e.input).exportEnum(e.inputKey))
    val exportModels = p.models.map(e => inputs(e.input).exportModel(e.inputKey))

    ExportConfiguration(project = p, enums = exportEnums, models = exportModels)
  }

  private[this] def export(config: ExportConfiguration) = {

  }
}
