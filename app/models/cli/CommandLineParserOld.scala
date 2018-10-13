/*
package models.cli

import models.command.ProjectileCommand
import models.command.ProjectileCommand._
import models.input.{InputSummary, InputTemplate}
import models.project.{ProjectSummary, ProjectTemplate}
import scopt._

object CommandLineParser {
  private[this] val disclaimer = "Unless you really like the command line, passing \"server\" is your best bet"

  val parser = new OptionParser[CommandLineOptions](util.Version.projectId) {
    head(util.Version.projectName, util.Version.version, "-", util.Config.slogan, s"\n\n$disclaimer\n")

    help("help").text("Prints this usage text\n")

    cmd("init").action((_, c) => c.withCommand(Init)).text("Creates the config directory and required files, if missing")
    cmd("doctor").action((_, c) => c.withCommand(Doctor)).text("Prints the project configurations, if present")
    cmd("server").action((_, c) => c.withCommand(StartServer())).text("Starts the web server so you don't have to use this crufty CLI").children(
      opt[Int]("port").text(s"Http port for the server, defaults to [${util.Version.projectPort}]").action((p, c) => c.withCommand(StartServer(p)))
    )
    cmd("add").text("Adds a basic configuration for inputs and projects").children(
      cmd("input").text("Add the input's definition").action((_, c) => c.withCommand(AddInput(InputSummary()))).children(
        arg[String]("type").text(s"Input type, any of [${InputTemplate.values.map(_.value).mkString(", ")}]").action { (x, c) =>
          withCommand[AddInput](c, ai => ai.copy(input = ai.input.copy(template = InputTemplate.withValue(x))))
        },
        arg[String]("key").text("Key that identifies this input").action { (x, c) =>
          withCommand[AddInput](c, ai => ai.copy(input = ai.input.copy(key = x)))
        },
        arg[String]("title").optional().text("Optional title for this input").action { (x, c) =>
          withCommand[AddInput](c, ai => ai.copy(input = ai.input.copy(title = x)))
        },
        arg[String]("description").optional().text("Optional description for this input").action { (x, c) =>
          withCommand[AddInput](c, ai => ai.copy(input = ai.input.copy(description = x)))
        }
      ),
      cmd("project").text("Removes the project's definition").action((_, c) => c.withCommand(AddProject(ProjectSummary()))).children(
        arg[String]("template").text(s"Project template, any of [${ProjectTemplate.values.map(_.value).mkString(", ")}]").action { (x, c) =>
          withCommand[AddProject](c, ap => ap.copy(project = ap.project.copy(template = ProjectTemplate.withValue(x))))
        },
        arg[String]("key").text("Key that identifies this project").action { (x, c) =>
          withCommand[AddProject](c, ap => ap.copy(project = ap.project.copy(key = x)))
        },
        arg[String]("title").optional().text("Optional title for this project").action { (x, c) =>
          withCommand[AddProject](c, ap => ap.copy(project = ap.project.copy(title = x)))
        },
        arg[String]("description").optional().text("Optional description for this project").action { (x, c) =>
          val cmd = c.command.get.asInstanceOf[AddProject]
          c.withCommand(cmd.copy(project = cmd.project.copy(description = x)))
        }
      )
    )
    cmd("get").text("Retrieves information from the configuration").children(
      cmd("project").text("Get the details of the projects within the application").action((_, c) => c.withCommand(ListProjects)).children(
        arg[String]("id").optional().text("Optional id of a project, will list all if not provided").action((x, c) => c.withCommand(GetProject(x)))
      ),
      cmd("input").text("Get the details of the inputs within the application").action((_, c) => c.withCommand(ListInputs)).children(
        arg[String]("id").optional().text("Optional id of an input, will list all if not provided").action((x, c) => c.withCommand(GetInput(x)))
      )
    )
    cmd("remove").abbr("rm").text("Removes cached data for inputs and projects").children(
      cmd("input").text("Removes the input's definition").children(
        arg[String]("id").text("Input id to remove").action((x, c) => c.withCommand(RemoveInput(x)))
      ),
      cmd("project").text("Removes the project's definition").children(
        arg[String]("id").text("Project id to remove").action((x, c) => c.withCommand(RemoveProject(x)))
      )
    )
    cmd("refresh").text("Updates cached input data, such as a database's definition").children(
      arg[String]("id").optional().text("Optional input id, will refresh all if not provided").action((x, c) => c.withCommand(RefreshInput(x)))
    )
    cmd("export").text("Runs the export for the project with the provided id").children(
      arg[String]("id").optional().text("Optional project id, will export all if not provided").action((x, c) => c.withCommand(ExportProject(x)))
    )
    cmd("batch").text("Runs each entry within a file").children(
      arg[String]("file").text("A file containing command line arguments on each line").action { (x, c) =>
        throw new IllegalStateException("Batch must be be handled outside the parser")
      }
    )
    cmd("testbed").hidden().action((_, c) => c.withCommand(Testbed)).text("Runs the local developmer testbed")
  }

  private[this] def withCommand[Cmd <: ProjectileCommand](c: CommandLineOptions, f: Cmd => Cmd) = {
    c.withCommand(f(c.command.get.asInstanceOf[Cmd]))
  }
}
*/