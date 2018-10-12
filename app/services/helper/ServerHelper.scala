package services.helper

import models.command.{ProjectileCommand, ProjectileResponse}
import models.command.ProjectileCommand.{StartServer, StopServer}
import models.command.ProjectileResponse.OK
import play.core.server.{RealServerProcess, Server}
import services.ProjectileService
import util.web.PlayServerHelper

trait ServerHelper { this: ProjectileService =>
  private[this] var serverOpt: Option[(RealServerProcess, Server)] = None

  protected val processServer: PartialFunction[ProjectileCommand, ProjectileResponse] = {
    case StartServer(port) => startServer(port)
    case StopServer => stopServer()
  }

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
