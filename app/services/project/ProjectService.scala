package services.project

import models.project.Project
import services.config.{ConfigService, FileCacheService}

class ProjectService(val cfg: ConfigService) extends FileCacheService[Project](cfg.projectDirectory, "Project") {
  override def newModel(key: String, title: String, description: String) = Project(key = key, title = title, description = description)

  refresh()
}
