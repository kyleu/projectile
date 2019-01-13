package com.kyleu.projectile.models.cli

import enumeratum.{CirceEnum, Enum, EnumEntry}
import com.kyleu.projectile.models.command.ProjectileCommand
import com.kyleu.projectile.models.input.InputSummary
import com.kyleu.projectile.models.project.ProjectSummary
import com.kyleu.projectile.util.Version
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

  // Common tasks
  object InputRefresh extends Command(name = "refresh", description = "Refreshes the provided input (or all)") with CommandLineAction {
    var key = arg[Option[String]](required = false)
    override def toCommand = ProjectileCommand.InputRefresh(key)
  }
  object ProjectUpdate extends Command(name = "update", description = "Updates the provided project (or all)") with CommandLineAction {
    var key = arg[Option[String]](required = false)
    override def toCommand = ProjectileCommand.ProjectUpdate(key)
  }
  object ProjectExport extends Command(name = "export", description = "Exports the provided project (or all)") with CommandLineAction {
    var key = arg[Option[String]](required = false)
    override def toCommand = ProjectileCommand.ProjectExport(key)
  }
  object Audit extends Command(name = "audit", description = "Audits the provided project (or all)") with CommandLineAction {
    override def toCommand = ProjectileCommand.Audit
  }
  object Codegen extends Command(name = "codegen", description = "Generates code for the provided projects (or all)") with CommandLineAction {
    var key = arg[Option[String]](required = false)
    override def toCommand = ProjectileCommand.Codegen(key.toSeq.flatMap(_.split(",").map(_.trim)))
  }

  // Inputs
  object InputList extends Command(name = "input", description = "Lists summaries of inputs, or details if key provided") with CommandLineAction {
    var key = arg[Option[String]](required = false)
    override def toCommand = ProjectileCommand.Inputs(key)
  }
  object InputAdd extends Command(name = "input-add", description = "Add an input to the system") with CommandLineAction {
    var key = arg[String]()
    var title = opt[Option[String]](description = "Optional title for this input")
    var desc = opt[Option[String]](description = "Optional description for this input")
    override def toCommand = ProjectileCommand.InputAdd(InputSummary(key = key, description = desc.getOrElse("")))
  }

  // Projects
  object ProjectList extends Command(name = "project", description = "Lists summaries of Projectile projects, or details of key") with CommandLineAction {
    var key = arg[Option[String]](required = false)
    override def toCommand = ProjectileCommand.Projects(key)
  }
  object ProjectAdd extends Command(name = "project-add", description = "Adds a project to the system") with CommandLineAction {
    var key = arg[String]()
    var title = opt[Option[String]](description = "Optional title for this project")
    var desc = opt[Option[String]](description = "Optional description for this project")
    override def toCommand = ProjectileCommand.ProjectAdd(ProjectSummary(key = key, description = desc.getOrElse("")))
  }

  object Server extends Command(name = "server", description = s"Starts the web application") with CommandLineAction {
    var port = opt[Int](description = s"Http port for the server", default = Version.projectPort)
    override def toCommand = ProjectileCommand.ServerStart(port)
  }

  override val values = findValues
  val actions = values.map(_.asInstanceOf[Command with CommandLineAction])
}
