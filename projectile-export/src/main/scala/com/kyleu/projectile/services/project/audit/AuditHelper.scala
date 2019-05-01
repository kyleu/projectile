package com.kyleu.projectile.services.project.audit

import better.files.File
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.project.ProjectOutput
import com.kyleu.projectile.models.project.audit.{AuditMessage, AuditResult}
import com.kyleu.projectile.services.ProjectileService
import com.kyleu.projectile.services.project.{ProjectExportService, ProjectStatusService}

trait AuditHelper { this: ProjectileService =>
  private[this] lazy val exportSvc = new ProjectExportService(this)

  def audit(inputs: Seq[(ExportConfiguration, ProjectOutput)], verbose: Boolean) = {
    ProjectAuditService.audit(this, inputs)
  }

  def auditKeys(keys: Seq[String], verbose: Boolean) = {
    val inputs = keys.map { key =>
      val cfg = configForProject(key)
      loadExportConfig(key) -> exportSvc.getOutput(projectRoot = cfg.workingDirectory, key = key, verbose = verbose)
    }
    audit(inputs, verbose)
  }

  def auditAll(verbose: Boolean) = auditKeys(listProjects().map(_.key), verbose)

  def fix(key: String, t: String, src: String, tgt: String): Seq[String] = {
    val msg = AuditMessage(project = key, srcModel = src, src = src, t = t, tgt = tgt, message = "")
    fixMessage(msg = msg)
  }

  private[this] def fixMessage(msg: AuditMessage, result: Option[AuditResult] = None): Seq[String] = {
    msg.t match {
      case "all" =>
        val auditResult = result.getOrElse(auditKeys(keys = listProjects().map(_.key), verbose = false))
        msg.src match {
          case "config" => auditResult.configMessages.flatMap(m => fixMessage(m, Some(auditResult)))
          case "output" => auditResult.outputMessages.flatMap(m => fixMessage(m, Some(auditResult)))
          case x => throw new IllegalStateException(s"Unhandled fix source [$x]")
        }
      case "orphan" => Seq(fixOrphan(rootCfg.workingDirectory, msg.src))
      case "status" => Seq(ProjectStatusService.fix(getProject(msg.project), msg.src, msg.tgt))
      case "unindexed" => Seq(fixUnindexed(msg.src, msg.tgt))
      case x => throw new IllegalStateException(s"I don't know how to fix a [$x] yet")
    }
  }

  private[this] def fixOrphan(dir: File, src: String) = {
    val f = dir / src
    if (f.isRegularFile && f.isWriteable) {
      f.delete(swallowIOExceptions = true)
      s"Removed file [$src]"
    } else {
      s"Cannot remove file [$src] from [${dir.pathAsString}]"
    }
  }

  private[this] def fixUnindexed(src: String, tgt: String) = {
    s"""create index if not exists "${src}_${tgt}_idx" on "$src" using btree ("$tgt" asc);"""
  }
}
