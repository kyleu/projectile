package services

import io.circe.Json
import models.command.{ProjectileCommand, ProjectileResponse}
import models.command.ProjectileCommand._
import models.command.ProjectileResponse._
import models.input.InputSummary
import models.project.ProjectSummary
import services.config.{ConfigService, ConfigValidator}
import services.input.InputService
import services.project.ProjectService
import util.web.PlayServerHelper
import util.JsonSerializers._
import play.core.server._

class ProjectileService(val path: String = ".") {
  private[this] val cfg = new ConfigService(path)

  private[this] val projectSvc = new ProjectService(cfg)
  private[this] val inputSvc = new InputService(cfg)

  private[this] var serverOpt: Option[(RealServerProcess, Server)] = None

  val rootDir = better.files.File(path)
  val fullPath = rootDir.pathAsString

  def process(cmd: ProjectileCommand, verbose: Boolean = false): ProjectileResponse = {
    import models.command.ProjectileCommand._

    cmd match {
      case Doctor => ConfigValidator.validate(new ConfigService(path), verbose)

      case StartServer(port) => startServer(port)
      case StopServer => stopServer()

      case ListProjects => ProjectileResponse.ProjectList(projectSvc.list())
      case GetProject(key) => ProjectileResponse.ProjectDetail(projectSvc.load(key))
      case AddProject(p) => ProjectileResponse.ProjectDetail(projectSvc.add(p))
      case RemoveProject(key) => ProjectileResponse.JsonResponse(projectSvc.remove(key).asJson)
      case ExportProject(key) => ProjectileResponse.JsonResponse(projectSvc.export(key).asJson)
      case AuditProject(key) => ProjectileResponse.JsonResponse(projectSvc.audit(key).asJson)

      case ListInputs => ProjectileResponse.InputList(inputSvc.list())
      case GetInput(key) => ProjectileResponse.InputDetail(inputSvc.load(key))
      case AddInput(i) => ProjectileResponse.InputDetail(inputSvc.add(i))
      case RemoveInput(key) => ProjectileResponse.InputList(Seq(inputSvc.remove(key)))
      case RefreshInput(key) => ProjectileResponse.InputDetail(inputSvc.refresh(key))

      case Testbed => JsonResponse(Json.True)

      case unhandled => throw new IllegalStateException(s"Unhandled action [$unhandled]")
    }
  }

  def testbed() = process(Testbed).asInstanceOf[JsonResponse]

  def listProjects() = process(ListProjects).asInstanceOf[ProjectList].projects
  def getProject(key: String) = process(GetProject(key)).asInstanceOf[ProjectDetail].project
  def addProject(summary: ProjectSummary) = process(AddProject(summary)).asInstanceOf[ProjectDetail].project
  def removeProject(key: String) = process(RemoveProject(key)).asInstanceOf[String]
  def exportProject(key: String) = process(ExportProject(key)).asInstanceOf[JsonResponse].json
  def auditProject(key: String) = process(AuditProject(key)).asInstanceOf[JsonResponse].json

  def listInputs() = process(ListInputs).asInstanceOf[InputList].inputs
  def getInput(key: String) = process(GetInput(key)).asInstanceOf[InputDetail].input
  def addInput(summary: InputSummary) = process(AddInput(summary)).asInstanceOf[InputDetail].input
  def removeInput(key: String) = process(RemoveInput(key)).asInstanceOf[String]
  def refreshInput(key: String) = process(RefreshInput(key)).asInstanceOf[InputDetail].input

  def startServer(port: Int) = {
    PlayServerHelper.setSvc(this)
    serverOpt = Some(PlayServerHelper.startServer(Some(port)))
    OK
  }

  def stopServer() = {
    serverOpt.getOrElse(throw new IllegalStateException("No server has been started"))._2.stop()
    serverOpt = None
    OK
  }
}
