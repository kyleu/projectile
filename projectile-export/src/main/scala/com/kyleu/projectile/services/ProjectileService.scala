package com.kyleu.projectile.services

import com.kyleu.projectile.models.cli.ServerHelper
import com.kyleu.projectile.models.command.ProjectileCommand._
import io.circe.Json
import com.kyleu.projectile.models.command.{ProjectileCommand, ProjectileResponse}
import com.kyleu.projectile.models.command.ProjectileResponse._
import com.kyleu.projectile.services.config.{ConfigService, ConfigValidator}
import com.kyleu.projectile.services.input.InputHelper
import com.kyleu.projectile.services.project.ProjectHelper
import com.kyleu.projectile.services.audit.AuditHelper
import com.kyleu.projectile.util.JsonSerializers._

class ProjectileService(val rootCfg: ConfigService = new ConfigService(".")) extends InputHelper with ProjectHelper with AuditHelper {
  val rootDir = better.files.File(rootCfg.path)
  val fullPath = rootDir.pathAsString

  def configForInput(key: String) = rootCfg.configForInput(key).getOrElse(throw new IllegalStateException(s"Cannot find input [$key]"))
  def configForProject(key: String) = rootCfg.configForProject(key).getOrElse(throw new IllegalStateException(s"Cannot find project [$key]"))

  def process(cmd: ProjectileCommand, verbose: Boolean = false): ProjectileResponse = {
    import com.kyleu.projectile.models.command.ProjectileCommand._

    val processCore: PartialFunction[ProjectileCommand, ProjectileResponse] = {
      case Init => init()
      case Doctor => doctor(verbose)
      case Testbed => testbed()
      case s: ServerStart => ServerHelper.inst match {
        case Some(srv) => srv.startServer(s.port)
        case None => throw new IllegalStateException("No server available")
      }
      case ServerStop => ServerHelper.inst match {
        case Some(srv) => srv.stopServer()
        case None => throw new IllegalStateException("No server available")
      }
    }

    processCore.orElse(processInput).orElse(processProject).apply(cmd)
  }

  def init() = rootCfg.init()
  def doctor(verbose: Boolean) = ConfigValidator.validate(rootCfg, verbose)
  def testbed() = JsonResponse(Json.True)

  protected val processProject: PartialFunction[ProjectileCommand, ProjectileResponse] = {
    case Projects(key) => key match {
      case Some(k) => ProjectDetail(getProject(k))
      case None => ProjectList(listProjects())
    }

    case ProjectAdd(p) => ProjectDetail(saveProject(p))
    case ProjectRemove(key) => removeProject(key)

    case SaveEnumMembers(p, members) => JsonResponse(saveEnumMembers(p, members).asJson)
    case RemoveEnumMember(p, member) => JsonResponse(removeEnumMember(p, member).asJson)

    case SaveModelMembers(p, members) => JsonResponse(saveModelMembers(p, members).asJson)
    case RemoveModelMember(p, member) => JsonResponse(removeModelMember(p, member).asJson)

    case SaveServiceMembers(p, members) => JsonResponse(saveServiceMembers(p, members).asJson)
    case RemoveServiceMember(p, member) => JsonResponse(removeServiceMember(p, member).asJson)

    case ProjectUpdate(key) => key match {
      case Some(k) => ProjectUpdateResult(k, updateProject(k))
      case None => CompositeResult(listProjects().map(p => ProjectUpdateResult(p.key, updateProject(p.key))))
    }
    case ProjectExport(key) => key match {
      case Some(k) => projectResults(k)
      case None => CompositeResult(listProjects().map(p => projectResults(p.key)))
    }
    case Audit => ProjectAuditResult(audit(listProjects().map(_.key), verbose = false))
    case ProjectCodegen(key) => key match {
      case Some(k) => codegenProject(k, verbose = false)
      case None => CompositeResult(listProjects().map(p => codegenProject(p.key, verbose = false)))
    }
  }

  override val toString = s"Projectile Service @ ${rootCfg.workingDirectory}"
}
