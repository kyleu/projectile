package com.kyleu.projectile.models.cli

import com.kyleu.projectile.models.command.ProjectileResponse
import com.kyleu.projectile.models.command.ProjectileResponse._
import com.kyleu.projectile.models.input.{Input, InputSummary}
import com.kyleu.projectile.models.project.audit.AuditResult
import com.kyleu.projectile.models.project.codegen.CodegenResult
import com.kyleu.projectile.models.project.{Project, ProjectOutput, ProjectSummary}
import com.kyleu.projectile.services.output.OutputService
import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.JacksonUtils.printJackson
import com.kyleu.projectile.util.tracing.TraceData

object CommandLineOutput extends Logging {
  def logResponse(r: ProjectileResponse) = logsFor(r).foreach(s => log.info(s)(TraceData.noop))

  def logsFor(r: ProjectileResponse): Seq[String] = r match {
    case OK => Seq("Success: OK")
    case Error(msg) => Seq(s"Error: $msg")
    case JsonResponse(json) => Seq(printJackson(json))

    case InputList(inputs) => inputs.map(logForInputSummary)
    case InputDetail(input) => Seq(logForInput(input))
    case InputResults(results) => results.map(r => logForInput(r.input))

    case ProjectList(projects) => projects.map(logForProjectSummary)
    case ProjectDetail(p) => Seq(logForProject(p))

    case ProjectUpdateResult(key, resp) => s"[$key] Updated:" +: resp.map(" - " + _)
    case ProjectExportResult(output, files) => logForExportResult(output, files)
    case ProjectAuditResult(result) => logForAuditResult(result)
    case ProjectCodegenResult(result) => logForCodegenResult(result)
    case CompositeResult(results) => logForCompositeResult(results)
  }

  private[this] def logForInputSummary(is: InputSummary) = s"[${is.key}]: ${is.template.title}"
  private[this] def logForInput(input: Input) = s"[${input.key}]: $input"

  private[this] def logForProjectSummary(ps: ProjectSummary) = s"[${ps.key}]: ${ps.template.title}"
  private[this] def logForProject(project: Project) = s"[${project.key}]: $project"

  def logForExportResult(output: ProjectOutput, files: Seq[OutputService.WriteResult]) = {
    val filesFiltered = files.filter(_.logs.nonEmpty)
    val fileMessages = if (filesFiltered.isEmpty) {
      Seq(" - No changes required")
    } else {
      filesFiltered.flatMap(f => s" - ${f.file}" +: f.logs.map(l => "   - " + l))
    }
    s"[${output.project.key}] Exported:" +: fileMessages
  }

  private[this] def logForAuditResult(result: AuditResult) = {
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
    s"Audit Result:" +: (cfgMsgs ++ outputMsgs)
  }

  private[this] def logForCodegenResult(result: CodegenResult) = {
    val filteredExportResults = result.exportResults.filter(er => er._2.exists(_.logs.nonEmpty))
    val exportMessages = if (filteredExportResults.isEmpty) {
      Nil
    } else {
      s"Exported [${filteredExportResults.size}] projects:" +: filteredExportResults.flatMap { er =>
        s"  ${er._1.project.key}:" +: er._2.filter(_.logs.nonEmpty).map(msg => s"  - ${msg.file}: ${msg.logs.mkString(", ")}")
      }
    }
    val auditMessages = result.auditResults.toSeq.flatMap { aud =>
      val msgs = aud.configMessages ++ aud.outputMessages
      if (msgs.isEmpty) {
        Nil
      } else {
        s"[${msgs.size}] audit messages:" +: msgs.map(m => "  - " + m.toString)
      }
    }
    result.updates ++ exportMessages ++ auditMessages
  }

  def logForCompositeResult(results: Seq[ProjectileResponse]): Seq[String] = results.size match {
    case 0 => Seq("No results")
    case 1 => logsFor(results.head)
    case _ => results.zipWithIndex.flatMap(r => s"Result [${r._2}]:" +: logsFor(r._1))
  }
}
