package models.cli

import models.command.ProjectileResponse
import models.command.ProjectileResponse._
import models.input.{Input, InputSummary}
import models.project.{Project, ProjectSummary}
import util.JsonSerializers._
import util.Logging

object CommandLineOutput extends Logging {
  def logResponse(r: ProjectileResponse) = log.info(logFor(r))

  def logFor(r: ProjectileResponse) = r match {
    case OK => "Success: OK"
    case Error(msg) => s"Error: $msg"
    case JsonResponse(json) => json.spaces2

    case InputDetail(input) => logForInput(input)
    case InputList(inputs) => inputs.map(logForInputSummary).mkString("\n")

    case ProjectDetail(p) => logForProject(p)
    case ProjectList(projects) => projects.map(logForProjectSummary).mkString("\n")

    case x => x.toString
  }

  private[this] def logForInputSummary(is: InputSummary) = s"[${is.key}]: ${is.title} (${is.template.title})"
  private[this] def logForInput(input: Input) = "Input!"

  private[this] def logForProjectSummary(ps: ProjectSummary) = s"[${ps.key}]: ${ps.title} (${ps.template.title})"
  private[this] def logForProject(project: Project) = "Project!"
}
