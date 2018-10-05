package models.cli

import models.command.ProjectileCommand

case class CommandLineOptions(
    command: Option[ProjectileCommand] = None,
    verbose: Boolean = false,
    workingDir: String = "."
) {
  def withCommand(cmd: ProjectileCommand) = this.copy(command = Some(cmd))
}
