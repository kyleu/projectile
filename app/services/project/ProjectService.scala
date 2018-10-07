package services.project

import better.files.File
import models.command.ProjectileResponse
import models.project.Project
import services.config.ConfigService

class ProjectService(val cfg: ConfigService) {
  val dir = cfg.projectDirectory

  def loadProject(projectDir: File) = {
    val key = projectDir.name.stripSuffix(".json")
    Project(key = key, title = "TODO", description = "TODO")
  }

  val projects = dir.children.filter(_.isDirectory).map(loadProject).toSeq

  def list() = ProjectileResponse.ProjectList(projects)

  def get(key: String) = ProjectileResponse.ProjectDetail(
    projects.find(_.key == key).getOrElse(throw new IllegalStateException(s"Cannot find configuration for project [$key]"))
  )
}
