package com.projectile.models.cli

import com.projectile.models.command.ProjectileResponse
import com.projectile.models.command.ProjectileResponse._
import com.projectile.models.input.{Input, InputSummary}
import com.projectile.models.project.{Project, ProjectSummary}
import com.projectile.util.Logging

object CommandLineOutput extends Logging {
  def logResponse(r: ProjectileResponse) = logFor(r).foreach(s => log.info(s))

  def logFor(r: ProjectileResponse): Seq[String] = r match {
    case OK => Seq("Success: OK")
    case Error(msg) => Seq(s"Error: $msg")
    case JsonResponse(json) => Seq(json.spaces2)

    case InputDetail(input) => Seq(logForInput(input))
    case InputList(inputs) => inputs.map(logForInputSummary)

    case ProjectDetail(p) => Seq(logForProject(p))
    case ProjectList(projects) => projects.map(logForProjectSummary)

    case x => Seq(x.toString)
  }

  private[this] def logForInputSummary(is: InputSummary) = s"[${is.key}]: ${is.title} (${is.template.title})"
  private[this] def logForInput(input: Input) = "Input!"

  private[this] def logForProjectSummary(ps: ProjectSummary) = s"[${ps.key}]: ${ps.title} (${ps.template.title})"
  private[this] def logForProject(project: Project) = "Project!"
}
