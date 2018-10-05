package util.web

import play.api._
import play.core.server.{ProdServerStart, RealServerProcess, ServerConfig, ServerProvider}

object PlayServerHelper {
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
