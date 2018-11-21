import com.projectile.models.cli.{CommandLineAction, CommandLineOutput}
import com.projectile.services.ProjectileService
import com.projectile.services.config.ConfigService
import com.projectile.util.Version.{projectId, projectName, version}
import com.projectile.util.{Logging, Version}
import org.backuity.clist.Cli

object ProjectileCLI extends Logging {
  def main(args: Array[String]): Unit = {
    val startMs = System.currentTimeMillis
    val result = if (args.headOption.contains("batch")) {
      runBatch(args).flatMap(_._2)
    } else {
      runArgs(args).toSeq
    }
    log.info(s"${Version.projectName} completed successfully in [${System.currentTimeMillis - startMs}ms]")
    result.foreach(CommandLineOutput.logResponse)
  }

  private[this] def runArgs(args: Array[String]) = parse(args).map { c =>
    new ProjectileService(new ConfigService(c.dir)).process(c.toCommand, c.verbose)
  }

  private[this] def runBatch(args: Array[String]) = {
    if (args.length != 2) {
      throw new IllegalStateException("When calling batch, please pass only a single path as the argument.")
    }
    val f = better.files.File(args(1))
    if (f.exists && f.isRegularFile && f.isReadable) {
      f.lines.toList.map(l => l -> runArgs(l.split(' ').map(_.trim).filter(_.nonEmpty)))
    } else {
      throw new IllegalStateException(s"Cannot read batch file [${f.pathAsString}]")
    }
  }

  def parse(args: Seq[String]) = {
    Cli.parse(args.toArray).withProgramName(projectId).version(version, projectName).exitCode(0).withCommands(CommandLineAction.actions: _*)
  }
}
