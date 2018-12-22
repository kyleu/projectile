package util.web

import com.kyleu.projectile.models.cli.ServerHelper
import com.kyleu.projectile.models.command.ProjectileResponse.OK
import com.kyleu.projectile.services.ProjectileService
import com.kyleu.projectile.services.config.ConfigService
import com.kyleu.projectile.util.Logging
import play.api._
import play.core.server._

object PlayServerHelper extends Logging with ServerHelper {
  private[this] val defaultPath = "."
  // private[this] val defaultPath = "/Users/kyle/Projects/Personal/boilerplay"
  // private[this] val defaultPath = "/Users/kyle/Projects/Fevo/coco"
  // private[this] val defaultPath = "/Users/kyle/Projects/Fevo/fevomatic"
  // private[this] val defaultPath = "/Users/kyle/Projects/Fevo/lib-slick"

  private[this] var serverOpt: Option[(RealServerProcess, Server)] = None
  private[this] var activeService: Option[ProjectileService] = None

  def setNewDirectory(path: String) = setSvc(new ProjectileService(new ConfigService(path)))

  def svc = activeService.getOrElse {
    setSvc(new ProjectileService(new ConfigService(defaultPath)))
    activeService.getOrElse(throw new IllegalStateException("Cannot initialize service"))
  }

  override def startServer(port: Int) = {
    serverOpt = Some(start(Some(port)))
    OK
  }

  override def stopServer() = {
    serverOpt.getOrElse(throw new IllegalStateException("No server has been started"))._2.stop()
    serverOpt = None
    OK
  }

  private[this] def setSvc(svc: ProjectileService) = {
    if (!svc.cfg.available) {
      log.info(s"Initializing [.projectile] config directory for [${svc.cfg.path}]")
      svc.cfg.init()
    }
    activeService = Some(svc)
    log.info(s"Set active service to $svc")
  }

  private[this] def start(port: Option[Int]) = {
    val process = new RealServerProcess(Nil)
    val baseConfig: ServerConfig = ProdServerStart.readServerConfigSettings(process)
    val config = baseConfig.copy(port = port.orElse(baseConfig.port))
    val application: Application = {
      val environment = Environment(config.rootDir, process.classLoader, Mode.Prod)
      val context = ApplicationLoader.createContext(environment)
      val loader = ApplicationLoader(context)
      loader.load(context)
    }
    Play.start(application)

    val serverProvider: ServerProvider = ServerProvider.fromConfiguration(process.classLoader, config.configuration)
    val server = serverProvider.createServer(config, application)
    process.addShutdownHook(server.stop())
    process -> server
  }
}
