package util.web

import play.api._
import play.core.server.{ProdServerStart, RealServerProcess, ServerConfig, ServerProvider}
import com.projectile.services.ProjectileService
import com.projectile.services.config.ConfigService
import com.projectile.util.Logging

object PlayServerHelper extends Logging {
  private[this] var activeService: Option[ProjectileService] = None

  def setSvc(svc: ProjectileService) = {
    activeService = Some(svc)
    log.info(s"Set active service to $svc")
  }
  def setNewDirectory(path: String) = setSvc(new ProjectileService(new ConfigService(path)))

  def svc = activeService.getOrElse {
    setSvc(new ProjectileService(new ConfigService(".")))
    activeService.getOrElse(throw new IllegalStateException("Cannot initialize service"))
  }

  def startServer(port: Option[Int]) = {
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
