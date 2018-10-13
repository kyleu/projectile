package services.project

import services.ProjectileService
import services.config.ConfigService

class ProjectExportService(val cfg: ConfigService) {
  def export(key: String, projectile: ProjectileService) = {
    val p = projectile.getProject(key)

    val inputs = p.allMembers.map(_.input).distinct.map(projectile.getInput).map(i => i.key -> i).toMap

    // TODO apply overrides
    val exportEnums = p.enums.map(e => inputs(e.input).exportEnum(e.inputKey))
    val exportModels = p.models.map(e => inputs(e.input).exportModel(e.inputKey))

    s"TODO: Project Export (${exportEnums.size} enums, ${exportModels.size} models)"
  }
}
