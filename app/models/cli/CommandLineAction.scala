package models.cli

import enumeratum.{CirceEnum, Enum, EnumEntry}
import models.command.ProjectileCommand
import models.input.InputSummary
import models.project.ProjectSummary
import org.backuity.clist.{Command, arg, opt}

sealed trait CommandLineAction extends EnumEntry { this: Command =>
  var verbose = opt[Boolean](abbrev = "v", description = "When set, logs way too much information", default = false)
  var dir = opt[String](description = "Working directory, defaults to \".\"", default = ".")

  def toCommand: ProjectileCommand
}

object CommandLineAction extends Enum[CommandLineAction] with CirceEnum[CommandLineAction] {
  object Doctor extends Command(name = "doctor", description = "Validates the app configuration, if present") with CommandLineAction {
    override def toCommand = ProjectileCommand.Doctor
  }

  object Init extends Command(name = "init", description = "Creates the config directory and required files, if missing") with CommandLineAction {
    override def toCommand = ProjectileCommand.Init
  }

  object InputList extends Command(name = "input-list", description = "Lists summaries of Projectile inputs") with CommandLineAction {
    override def toCommand = ProjectileCommand.ListInputs
  }
  object InputAdd extends Command(name = "input-add", description = "Add an input to the system") with CommandLineAction {
    var key = arg[String]()
    var title = opt[Option[String]](description = "Optional title for this input")
    var desc = opt[Option[String]](description = "Optional description for this input")
    override def toCommand = ProjectileCommand.AddInput(InputSummary(key = key, title = title.getOrElse(key), description = desc.getOrElse("")))
  }
  object InputGet extends Command(name = "input-get", description = "Prints details of the input with the provided key") with CommandLineAction {
    var key = arg[String]()
    override def toCommand = ProjectileCommand.GetInput(key)
  }
  object InputRefresh extends Command(name = "input-refresh", description = "Refreshes the input with the provided key") with CommandLineAction {
    var key = arg[String]()
    override def toCommand = ProjectileCommand.RefreshInput(key)
  }

  object ProjectList extends Command(name = "project-list", description = "Lists summaries of Projectile projects") with CommandLineAction {
    override def toCommand = ProjectileCommand.ListProjects
  }
  object ProjectAdd extends Command(name = "project-add", description = "Adds a project to the system") with CommandLineAction {
    var key = arg[String]()
    var title = opt[Option[String]](description = "Optional title for this project")
    var desc = opt[Option[String]](description = "Optional description for this project")
    override def toCommand = ProjectileCommand.AddProject(ProjectSummary(key = key, title = title.getOrElse(key), description = desc.getOrElse("")))
  }
  object ProjectGet extends Command(name = "project-get", description = "Prints details of the project with the provided key") with CommandLineAction {
    var key = arg[String]()
    override def toCommand = ProjectileCommand.GetProject(key)
  }
  object ProjectExport extends Command(name = "project-export", description = "Exports the Projectile project with the provided key") with CommandLineAction {
    var key = arg[String]()
    override def toCommand = ProjectileCommand.ExportProject(key)
  }
  object ProjectAudit extends Command(name = "project-audit", description = "Audits the Projectile project with the provided key") with CommandLineAction {
    var key = arg[String]()
    override def toCommand = ProjectileCommand.AuditProject(key)
  }

  object Server extends Command(name = "server", description = s"Starts the web application") with CommandLineAction {
    var port = opt[Int](description = s"Http port for the server", default = util.Version.projectPort)
    override def toCommand = ProjectileCommand.StartServer(port)
  }

  override val values = findValues
  val actions = values.map(_.asInstanceOf[Command with CommandLineAction])
}
