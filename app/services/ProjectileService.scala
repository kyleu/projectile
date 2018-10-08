package services

import io.circe.Json
import models.command.{ProjectileCommand, ProjectileResponse}
import models.command.ProjectileCommand._
import models.command.ProjectileResponse._
import services.config.{ConfigService, ConfigValidator}
import services.input.InputService
import services.project.ProjectService
import util.web.PlayServerHelper
import play.core.server._

class ProjectileService(val path: String = ".") {
  private[this] val cfg = new ConfigService(path)

  private[this] val projectSvc = new ProjectService(cfg)
  private[this] val inputSvc = new InputService(cfg)

  private[this] var serverOpt: Option[(RealServerProcess, Server)] = None

  val fullPath = better.files.File(path).pathAsString

  def process(cmd: ProjectileCommand, verbose: Boolean = false) = {
    import models.command.ProjectileCommand._

    cmd match {
      case Doctor => ConfigValidator.validate(new ConfigService(path), verbose)

      case StartServer(port) => startServer(port)
      case StopServer => stopServer()

      case ListProjects => ProjectileResponse.ProjectList(projectSvc.list())
      case AddProject(p) => projectSvc.save(p)
      case RemoveProject(key) => projectSvc.remove(key)
      case GetProject(key) => ProjectileResponse.ProjectDetail(projectSvc.load(key))

      case ListInputs => ProjectileResponse.InputList(inputSvc.list())
      case AddInput(i) => inputSvc.save(i)
      case RemoveInput(key) => inputSvc.remove(key)
      case GetInput(key) => ProjectileResponse.InputDetail(inputSvc.load(key))
      case RefreshInput(key) => ProjectileResponse.InputDetail(inputSvc.refresh(key))

      case Testbed => JsonResponse(Json.True)

      case unhandled => throw new IllegalStateException(s"Unhandled action [$unhandled]")
    }
  }

  def testbed() = process(Testbed).asInstanceOf[JsonResponse]

  def listProjects() = process(ListProjects).asInstanceOf[ProjectList].projects
  def getProject(key: String) = process(GetProject(key)).asInstanceOf[ProjectDetail].project
  def exportProject(key: String) = process(ExportProject(key)).asInstanceOf[String]

  def listInputs() = process(ListInputs).asInstanceOf[InputList].inputs
  def getInput(key: String) = process(GetInput(key)).asInstanceOf[InputDetail].input
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
