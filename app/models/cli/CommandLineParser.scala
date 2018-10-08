package models.cli

import models.command.ProjectileCommand._
import scopt._

object CommandLineParser {
  private[this] val disclaimer = "Unless you really like the command line, passing \"server\" is your best bet"

  val parser = new OptionParser[CommandLineOptions](util.Version.projectId) {
    head(util.Version.projectName, util.Version.version, "-", util.Config.slogan, s"\n\n$disclaimer\n")

    opt[String]('d', "dir").action((x, c) => c.copy(workingDir = x)).text("working directory, defaults to \".\"")
    opt[Unit]('v', "verbose").action((_, c) => c.copy(verbose = true)).text("when set, logs way too much information")
    help("help").text("prints this usage text\n")

    cmd("init").action((_, c) => c.withCommand(Init)).text("creates the config directory and required files, if missing")
    cmd("doctor").action((_, c) => c.withCommand(Doctor)).text("prints the project configurations, if present")
    cmd("server").action((_, c) => c.withCommand(StartServer())).text("starts the web server so you don't have to use this crufty CLI").children(
      opt[Int]("port").text(s"Http port for the server, defaults to [${util.Version.projectPort}]").action((p, c) => c.withCommand(StartServer(p)))
    )
    cmd("get").text("retrieves information from the configuration").children(
      cmd("project").text("get the details of the projects within the application").action((_, c) => c.withCommand(ListProjects)).children(
        arg[String]("<id>").optional().text("optional id of a project, will list all if not provided").action((x, c) => c.withCommand(GetProject(x)))
      ),
      cmd("input").text("get the details of the inputs within the application").action((_, c) => c.withCommand(ListInputs)).children(
        arg[String]("<id>").optional().text("optional id of an input, will list all if not provided").action((x, c) => c.withCommand(GetInput(x)))
      )
    )
    cmd("refresh").text("updates cached data for inputs and projects").children(
      cmd("input").text("reload the the input's definition").action((_, c) => c.withCommand(ListInputs)).children(
        arg[String]("<id>").optional().text("optional input id, will refresh all if not provided").action((x, c) => c.withCommand(RefreshInput(x)))
      )
    )
    cmd("remove").abbr("rm").text("removes cached data for inputs and projects").children(
      cmd("input").text("reload the the input's definition").action((_, c) => c.withCommand(ListInputs)).children(
        arg[String]("<id>").optional().text("optional input id, will refresh all if not provided").action((x, c) => c.withCommand(RemoveInput(x)))
      )
    )
    cmd("batch").text("runs each entry within a file").children(
      arg[String]("file").text("a file containing command line arguments on each line").action { (x, c) =>
        throw new IllegalStateException("Batch must be be handled outside the parser")
      }
    )
    cmd("testbed").hidden().action((_, c) => c.withCommand(Testbed)).text("runs the local developmer testbed")
  }
}
