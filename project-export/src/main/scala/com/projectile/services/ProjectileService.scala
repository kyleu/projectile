package com.projectile.services

import com.projectile.models.cli.ServerHelper
import io.circe.Json
import com.projectile.models.command.{ProjectileCommand, ProjectileResponse}
import com.projectile.models.command.ProjectileResponse._
import com.projectile.services.config.{ConfigService, ConfigValidator}
import com.projectile.services.input.InputHelper
import com.projectile.services.project.ProjectHelper

class ProjectileService(val cfg: ConfigService = new ConfigService(".")) extends InputHelper with ProjectHelper {
  val rootDir = better.files.File(cfg.path)
  val fullPath = rootDir.pathAsString

  def process(cmd: ProjectileCommand, verbose: Boolean = false): ProjectileResponse = {
    import com.projectile.models.command.ProjectileCommand._

    val processCore: PartialFunction[ProjectileCommand, ProjectileResponse] = {
      case Init => init()
      case Doctor => doctor(verbose)
      case Testbed => testbed()
      case s: StartServer => ServerHelper.inst match {
        case Some(srv) => srv.startServer(s.port)
        case None => throw new IllegalStateException("No server available")
      }
      case StopServer => ServerHelper.inst match {
        case Some(srv) => srv.stopServer()
        case None => throw new IllegalStateException("No server available")
      }
    }

    processCore.orElse(processInput).orElse(processProject).apply(cmd)
  }

  def init() = cfg.init()
  def doctor(verbose: Boolean) = ConfigValidator.validate(cfg, verbose)
  def testbed() = JsonResponse(Json.True)

  override val toString = s"Projectile Service @ ${cfg.workingDirectory}"
}
