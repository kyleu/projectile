package util

object Config {
  final val projectId = Version.projectId
  final val projectName = Version.projectName
  final val version = Version.version
  final val slogan = "It makes projects"
  final val metricsId = projectId.replaceAllLiterally("-", "_")
  final val projectUrl = "https://github.com/KyleU/projectile"
  final val adminEmail = "projectile@kyleu.com"
  final val pageSize = 100
}
