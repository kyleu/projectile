package com.kyleu.projectile.services

import com.kyleu.projectile.models.cli.ServerHelper
import com.kyleu.projectile.models.command.ProjectileCommand._
import com.kyleu.projectile.models.command.ProjectileResponse._
import com.kyleu.projectile.models.command.{ProjectileCommand, ProjectileResponse}
import com.kyleu.projectile.services.config.ConfigService
import com.kyleu.projectile.services.input.InputHelper
import com.kyleu.projectile.services.project.{ProjectExampleService, ProjectHelper}
import com.kyleu.projectile.services.project.audit.AuditHelper
import com.kyleu.projectile.services.project.codegen.CodegenHelper
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.Json

class ProjectileService(val rootCfg: ConfigService = new ConfigService(".")) extends InputHelper with ProjectHelper with CodegenHelper with AuditHelper {
  val rootDir = better.files.File(rootCfg.path)
  val fullPath = rootDir.pathAsString

  def configForInput(key: String) = rootCfg.configForInput(key).getOrElse(throw new IllegalStateException(s"Cannot find input [$key]"))
  def configForProject(key: String) = rootCfg.configForProject(key).getOrElse(throw new IllegalStateException(s"Cannot find project [$key]"))

  def process(cmd: ProjectileCommand, verbose: Boolean = false): ProjectileResponse = {
    import com.kyleu.projectile.models.command.ProjectileCommand._

    val processCore: PartialFunction[ProjectileCommand, ProjectileResponse] = {
      case Init => init()
      case Testbed => testbed()
      case s: ServerStart => ServerHelper.inst match {
        case Some(srv) => srv.startServer(s.port)
        case None => serverHelp()
      }
      case ServerStop => ServerHelper.inst match {
        case Some(srv) => srv.stopServer()
        case None => serverHelp()
      }
      case CreateExample(key, template, force) => createExample(key, template, force)
    }

    processCore.orElse(processInput).orElse(processProject).apply(cmd)
  }

  def init() = rootCfg.init()
  def testbed() = JsonResponse(Json.True)

  def serverHelp() = Error(ServerHelper.sbtError)

  def createExample(key: String, template: String, force: Boolean) = {
    ProjectExampleService.projects.find(_.key == template).getOrElse {
      throw new IllegalStateException(s"Template [$key}] must be one of [${ProjectExampleService.projects.map(_.key).mkString(", ")}]")
    }
    ProjectExampleService.extract(project = template, to = rootDir, name = key, force = force)
    ProjectileResponse.OK(s"Example project [$key] created in directory [${rootDir.pathAsString}] from template [$template]")
  }

  protected val processProject: PartialFunction[ProjectileCommand, ProjectileResponse] = {
    case Projects(key) => key match {
      case Some(k) => ProjectDetail(getProject(k))
      case None => ProjectList(listProjects())
    }

    case ProjectAdd(p) => ProjectDetail(saveProject(p))
    case ProjectRemove(key) => removeProject(key)

    case SetFeature(p, feature) => OK(s"Turned ${(if (setFeature(p, feature)) { "on" } else { "off" })} feature [$feature] for project [$p]")
    case SetPackage(p, item, pkg) => OK(setPackage(p, item, pkg))

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
    case Audit(f) => if (f) {
      val fixed = fix(key = "all", t = "all", src = "output", tgt = "")
      ProjectAuditResult(auditAll(verbose = false), fixed = fixed)
    } else {
      ProjectAuditResult(auditAll(verbose = false), fixed = Nil)
    }
    case Codegen(keys) => ProjectCodegenResult(codegen(keys, verbose = false))
  }

  override val toString = s"Projectile Service @ ${rootCfg.workingDirectory}"
}
