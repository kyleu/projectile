package com.kyleu.projectile.services.audit

import better.files.File
import com.kyleu.projectile.models.project.audit.{AuditMessage, AuditResult}
import com.kyleu.projectile.services.ProjectileService
import com.kyleu.projectile.services.project.ProjectExportService

trait AuditHelper { this: ProjectileService =>
  private[this] lazy val exportSvc = new ProjectExportService(this)

  def audit(keys: Seq[String], verbose: Boolean) = {
    val inputs = keys.map(key => loadConfig(key) -> exportSvc.getOutput(projectRoot = cfg.workingDirectory, key = key, verbose = verbose))
    ProjectAuditService.audit(cfg.workingDirectory, inputs)
  }

  def fix(key: String, t: String, src: String, tgt: String): String = {
    val msg = AuditMessage(project = key, srcModel = src, src = src, t = t, tgt = tgt, message = "")
    fixMessage(msg = msg)
  }

  private[this] def fixMessage(msg: AuditMessage, result: Option[AuditResult] = None): String = {
    msg.t match {
      case "all" =>
        val auditResult = result.getOrElse(audit(keys = listProjects().map(_.key), verbose = false))
        msg.src match {
          case "config" => auditResult.configMessages.map(m => fixMessage(m, Some(auditResult))).mkString(", ")
          case "output" => auditResult.outputMessages.map(m => fixMessage(m, Some(auditResult))).mkString(", ")
          case x => throw new IllegalStateException(s"Unhandled fix source [$x]")
        }
      case "orphan" => fixOrphan(cfg.workingDirectory, msg.src)
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
}
