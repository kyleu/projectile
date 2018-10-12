import models.cli.{CommandLineOptions, CommandLineOutput, CommandLineParser}
import services.ProjectileService
import services.config.ConfigService
import util.Logging

object ProjectileCLI extends Logging {
  def main(args: Array[String]): Unit = {
    val startMs = System.currentTimeMillis
    val result = if (args.headOption.contains("batch")) {
      runBatch(args).flatMap(_._2)
    } else {
      runArgs(args).toSeq
    }
    log.info(s"${util.Version.projectName} completed successfully in [${System.currentTimeMillis - startMs}ms]")
    result.foreach(CommandLineOutput.logResponse)
  }

  private[this] def runArgs(args: Seq[String], svc: Option[ProjectileService] = None) = {
    CommandLineParser.parser.parse(args, CommandLineOptions()) match {
      case Some(opts) => execute(svc.getOrElse(new ProjectileService(new ConfigService(opts.workingDir))), opts)
      case None => None // Noop, error already displayed
    }
  }

  private[this] def execute(svc: ProjectileService, opts: CommandLineOptions) = opts.command match {
    case None =>
      System.out.print(CommandLineParser.parser.renderTwoColumnsUsage + "\n")
      None
    case Some(cmd) => Some(svc.process(cmd, opts.verbose))
  }

  private[this] def runBatch(args: Array[String]) = {
    if (args.length != 2) {
      throw new IllegalStateException("When calling batch, please pass only a single path as the argument.")
    }
    val f = better.files.File(args(1))
    if (f.exists && f.isRegularFile && f.isReadable) {
      val svc = new ProjectileService(new ConfigService("."))
      f.lines.toList.map { l =>
        l -> runArgs(l.split(' ').map(_.trim).filter(_.nonEmpty), Some(svc))
      }
    } else {
      throw new IllegalStateException(s"Cannot read batch file [${f.pathAsString}]")
    }
  }
}
