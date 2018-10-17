package services.config

import models.command.ProjectileResponse

class ConfigService(val path: String) {
  val workingDirectory = better.files.File.apply(path)
  val configDirectory = workingDirectory / ".projectile"

  val inputDirectory = configDirectory / "input"

  val projectDirectory = configDirectory / "project"
  def projectDir(key: String) = {
    val dir = projectDirectory / key
    if (dir.exists && dir.isDirectory && dir.isWriteable) {
      dir
    } else {
      throw new IllegalStateException(s"Cannot load project dir for [$key]")
    }
  }

  val available = inputDirectory.isDirectory && inputDirectory.isWriteable && projectDirectory.isDirectory && projectDirectory.isWriteable

  def init() = {
    inputDirectory.createDirectories()
    projectDirectory.createDirectories()
    ProjectileResponse.OK
  }
}
