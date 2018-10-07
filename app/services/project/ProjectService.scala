package services.project

import models.project.ProjectSummary
import models.project.template.ProjectTemplate
import services.config.{ConfigService, FileCacheService}

class ProjectService(val cfg: ConfigService) extends FileCacheService[ProjectSummary](cfg.projectDirectory, "Project") {
  override def newModel(key: String, title: String, description: String) = {
    ProjectSummary(template = ProjectTemplate.Simple, key = key, title = title, description = description)
  }

  refresh()
}
