import com.projectile.ProjectileCLI
import com.projectile.models.cli.CommandLineOutput
import com.projectile.util.{Logging, Version}

object CLI extends Logging {
  def main(args: Array[String]): Unit = {
    val startMs = System.currentTimeMillis
    val result = if (args.headOption.contains("batch")) {
      ProjectileCLI.runBatch(args).flatMap(_._2)
    } else {
      ProjectileCLI.runArgs(args).toSeq
    }
    log.info(s"${Version.projectName} completed successfully in [${System.currentTimeMillis - startMs}ms]")
    result.foreach(CommandLineOutput.logResponse)
  }
}
