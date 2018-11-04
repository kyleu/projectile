package services

import io.circe.Json
import models.command.{ProjectileCommand, ProjectileResponse}
import models.command.ProjectileCommand._
import models.command.ProjectileResponse._
import services.config.{ConfigService, ConfigValidator}
import services.helper.{InputHelper, ProjectHelper, ServerHelper}

class ProjectileService(val cfg: ConfigService = new ConfigService(".")) extends InputHelper with ProjectHelper with ServerHelper {
  val rootDir = better.files.File(cfg.path)
  val fullPath = rootDir.pathAsString

  def process(cmd: ProjectileCommand, verbose: Boolean = false): ProjectileResponse = {
    import models.command.ProjectileCommand._

    val processCore: PartialFunction[ProjectileCommand, ProjectileResponse] = {
      case Init => cfg.init()
      case Doctor => ConfigValidator.validate(cfg, verbose)
      case Testbed => JsonResponse(Json.True)
    }

    processCore.orElse(processInput).orElse(processProject).orElse(processServer).apply(cmd)
  }

  def init() = process(Init)
  def doctor() = process(Doctor)
  def testbed() = process(Testbed).asInstanceOf[JsonResponse]

  override val toString = s"Projectile Service @ ${cfg.workingDirectory}"
}
