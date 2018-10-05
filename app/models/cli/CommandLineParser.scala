package models.cli

import models.command.ProjectileCommand._
import scopt._

object CommandLineParser {
  private[this] val disclaimer = s"""Unless you really like the command line, passing "server" is your best bet"""

  val parser = new OptionParser[CommandLineOptions](util.Version.projectId) {
    head(util.Version.projectName, util.Version.version, "- " + util.Config.slogan, s"\n\n$disclaimer\n")

    opt[String]('d', "dir").action((x, c) => c.copy(workingDir = x)).text("working directory, defaults to \".\"")
    opt[Unit]('v', "verbose").action((_, c) => c.copy(verbose = true)).text("when set, logs way too much information")
    help("help").text("prints this usage text\n")

    cmd("init").action((_, c) => c.withCommand(Init)).text("creates the config directory and required files, if missing")
    cmd("doctor").action((_, c) => c.withCommand(Doctor)).text("prints the project configurations, if present")
    cmd("server").action { (_, c) => c.withCommand(StartServer()) }.text("starts the web server so you don't have to use this crufty CLI").children(
      opt[Int]("port").text(s"Http port for the server, defaults to [${util.Version.projectPort}]").action((p, c) => c.withCommand(StartServer(p)))
    )
  }
}
