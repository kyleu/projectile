import models.cli.{CommandLineOptions, CommandLineParser}
import models.command.ProjectileCommand
import services.config.{ConfigService, ConfigValidator}
import services.input.InputService
import services.project.ProjectService
import util.Logging
import util.web.PlayServerHelper

object ProjectileCLI extends Logging {
  private[this] var activePath: Option[String] = None
  private[this] var activeServices: Option[(ConfigService, ProjectService, InputService)] = None

  def initialized = activeServices.isDefined

  def initServices(path: String, overwrite: Boolean = false) = {
    activePath.foreach {
      case active if !overwrite => throw new IllegalStateException("ControllerServices has already been initialized.")
      case _ => // noop
    }
    activePath = Some(path)
    val cfg = new ConfigService(path)
    activeServices = Some((cfg, new ProjectService(cfg), new InputService(cfg)))
  }

  def main(args: Array[String]): Unit = CommandLineParser.parser.parse(args, CommandLineOptions()) match {
    case Some(opts) => opts.command match {
      case None => System.out.print(CommandLineParser.parser.renderTwoColumnsUsage + "\n")
      case Some(cmd) =>
        if (opts.verbose) {
          log.info(s"Starting [${util.Version.projectName}] in path [${opts.workingDir}] with command line arguments [${args.mkString(" ")}]")
        }
        initServices(opts.workingDir)
        process(cmd, opts.verbose)
    }
    case None => // Noop, error already displayed
  }

  def process(cmd: ProjectileCommand, verbose: Boolean) = {
    import models.command.ProjectileCommand._

    if (cmd == Doctor) {
      ConfigValidator.validate(new ConfigService(activePath.getOrElse(".")), verbose)
    } else {
      if (activeServices.isEmpty) {
        initServices(".")
      }
      cmd match {
        case StartServer(port) => PlayServerHelper.startServer(Some(port))
        case unhandled => throw new IllegalStateException(s"Unhandled action [$unhandled]")
      }
    }
  }
}
