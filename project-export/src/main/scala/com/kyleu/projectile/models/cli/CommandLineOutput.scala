package com.kyleu.projectile.models.cli

import com.kyleu.projectile.models.command.ProjectileResponse
import com.kyleu.projectile.models.command.ProjectileResponse._
import com.kyleu.projectile.models.input.{Input, InputSummary}
import com.kyleu.projectile.models.project.{Project, ProjectSummary}
import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.JsonSerializers.printJson

object CommandLineOutput extends Logging {
  def logResponse(r: ProjectileResponse) = logFor(r).foreach(s => log.info(s))

  def logFor(r: ProjectileResponse): Seq[String] = r match {
    case OK => Seq("Success: OK")
    case Error(msg) => Seq(s"Error: $msg")
    case JsonResponse(json) => Seq(printJson(json))

    case InputList(inputs) => inputs.map(logForInputSummary)
    case InputDetail(input) => Seq(logForInput(input))
    case InputResults(results) => results.map(r => logForInput(r.input))

    case ProjectList(projects) => projects.map(logForProjectSummary)
    case ProjectDetail(p) => Seq(logForProject(p))

    case ProjectUpdateResult(key, resp) => s"[$key] Updated:" +: resp.map(" - " + _)
    case ProjectExportResult(key, files) => s"[$key] Exported:" +: files.filter(_.logs.nonEmpty).flatMap(f => s" - $f.file" +: f.logs.map("   - " + _))
    case ProjectAuditResult(result) =>
      val cfgMsgs = if (result.configMessages.isEmpty) {
        Nil
      } else {
        s" - [${result.configMessages.size}] Config Messages:" +: result.configMessages.map(m => s"   - ${m.tgt}: ${m.message}")
      }
      val outputMsgs = if (result.outputMessages.isEmpty) {
        Nil
      } else {
        s" - [${result.outputMessages.size}] Output Messages:" +: result.outputMessages.map(m => s"   - ${m.tgt}: ${m.message}")
      }
      s"[${result.config.project.key}] Audited:" +: (cfgMsgs ++ outputMsgs)

    case CompositeResult(results) => results.flatMap(logFor)
  }

  private[this] def logForInputSummary(is: InputSummary) = s"[${is.key}]: ${is.title} (${is.template.title})"
  private[this] def logForInput(input: Input) = s"[${input.key}]: $input"

  private[this] def logForProjectSummary(ps: ProjectSummary) = s"[${ps.key}]: ${ps.title} (${ps.template.title})"
  private[this] def logForProject(project: Project) = s"[${project.key}]: $project"
}
