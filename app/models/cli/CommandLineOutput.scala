package models.cli

import models.command.ProjectileResponse
import models.command.ProjectileResponse._
import models.input.{Input, InputSummary}
import models.project.{Project, ProjectSummary}
import util.JsonSerializers._
import util.Logging

object CommandLineOutput extends Logging {
  def logResponse(r: ProjectileResponse) = log.info(logFor(r))

  def logForInputSummary(inputSummary: InputSummary) = inputSummary.asJson.spaces2
  def logForInput(input: Input) = "Input!"

  def logForProjectSummary(projectSummary: ProjectSummary) = projectSummary.asJson.spaces2
  def logForProject(project: Project) = "Project!"

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
}
