import com.kyleu.projectile.ProjectileCLI
import com.kyleu.projectile.models.cli.CommandLineOutput
import com.kyleu.projectile.util.Version

object CLI {
  def main(args: Array[String]): Unit = {
    val startMs = System.currentTimeMillis
    val result = if (args.headOption.contains("batch")) {
      ProjectileCLI.runBatch(args).flatMap(_._2)
    } else {
      ProjectileCLI.runArgs(args.toIndexedSeq).toSeq
    }
    println(s"${Version.projectName} ${Version.version} completed successfully in [${System.currentTimeMillis - startMs}ms]")
    result.foreach(CommandLineOutput.logResponse)
  }
}
