package services.config

class ConfigService(val path: String) {
  val workingDirectory = better.files.File.apply(path)
  val configDirectory = workingDirectory / ".projectile"

  val inputDirectory = configDirectory / "input"
  val projectDirectory = configDirectory / "project"

  val available = inputDirectory.isDirectory && inputDirectory.isWriteable && projectDirectory.isDirectory && projectDirectory.isWriteable

  def init() = {
    inputDirectory.createDirectories()
    projectDirectory.createDirectories()
  }
}
