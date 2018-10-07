package services

import models.command.ProjectileCommand
import models.command.ProjectileCommand._
import models.command.ProjectileResponse._
import services.config.{ConfigService, ConfigValidator}
import services.input.InputService
import services.project.ProjectService
import util.web.PlayServerHelper
import play.core.server._

class ProjectileService(path: String = ".") {
  private[this] val cfg = new ConfigService(path)
  private[this] val projectSvc = new ProjectService(cfg)
  private[this] val inputSvc = new InputService(cfg)

  private[this] var serverOpt: Option[(RealServerProcess, Server)] = None

  def process(cmd: ProjectileCommand, verbose: Boolean = false) = {
    import models.command.ProjectileCommand._

    cmd match {
      case Doctor => ConfigValidator.validate(new ConfigService(path), verbose)
      case StartServer(port) => startServer(port)
      case StopServer => stopServer()
      case ListProjects => projectSvc.list()
      case GetProject(key) => projectSvc.get(key)
      case ListInputs => inputSvc.list()
      case GetInput(key) => inputSvc.get(key)
      case unhandled => throw new IllegalStateException(s"Unhandled action [$unhandled]")
    }
  }

  def listProjects() = process(ListProjects).asInstanceOf[ProjectList].projects
  def getProject(key: String) = process(GetProject(key)).asInstanceOf[ProjectDetail].project

  def listInputs() = process(ListInputs).asInstanceOf[InputList].inputs
  def getInput(key: String) = process(GetInput(key)).asInstanceOf[InputDetail].input

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
