import models.cli.{CommandLineOptions, CommandLineParser}
import services.ProjectileService
import util.Logging

object ProjectileCLI extends Logging {
  def main(args: Array[String]): Unit = if (args.headOption.contains("batch")) {
    runBatch(args)
  } else {
    parse(args)
  }

  private[this] def parse(args: Seq[String], svc: Option[ProjectileService] = None) = {
    CommandLineParser.parser.parse(args, CommandLineOptions()) match {
      case Some(opts) => execute(svc.getOrElse(new ProjectileService(opts.workingDir)), opts)
      case None => // Noop, error already displayed
    }
  }

  private[this] def execute(svc: ProjectileService, opts: CommandLineOptions) = opts.command match {
    case None => System.out.print(CommandLineParser.parser.renderTwoColumnsUsage + "\n")
    case Some(cmd) => svc.process(cmd, opts.verbose)
  }

  private[this] def runBatch(args: Array[String]) = {
    if (args.length != 2) {
      throw new IllegalStateException("When calling batch, please pass only a single path as the argument.")
    }
    val f = better.files.File(args(1))
    if (f.exists && f.isRegularFile && f.isReadable) {
      val svc = new ProjectileService(".")
      f.lines.toSeq.foreach { l =>
        parse(l.split('\n').map(_.trim), Some(svc))
      }
    } else {
      throw new IllegalStateException(s"Cannot read batch file [${f.pathAsString}]")
    }
  }
}
