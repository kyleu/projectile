package com.kyleu.projectile.services.config

import better.files.File
import com.kyleu.projectile.models.command.ProjectileResponse
import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.tracing.TraceData

object ConfigValidator extends Logging {
  def validate(svc: ConfigService, verbose: Boolean) = {
    val startMs = System.currentTimeMillis

    if (verbose) {
      log.info("Checking configuration and verifying files...")(TraceData.noop)
    }

    checkDir(svc.workingDirectory, "Working").orElse(checkDir(svc.configDirectory, "Config")) match {
      case Some(err) => error(err)
      case None => checkDir(svc.projectDirectory, "Project").orElse(checkDir(svc.inputDirectory, "Input")) match {
        case Some(err) => error(err)
        case None => alrightyThen(startMs, verbose)
      }
    }
  }

  private[this] def checkDir(dir: File, title: String) = if (dir.exists && dir.isDirectory) {
    if (dir.isWriteable) {
      None
    } else {
      Some(s"$title directory [${dir.path}] is not writeable")
    }
  } else {
    Some(s"$title directory [${dir.path}] does not exist")
  }

  private[this] def error(err: String) = {
    log.error(err)(TraceData.noop)
    ProjectileResponse.Error(err)
  }

  private[this] def alrightyThen(startMs: Long, verbose: Boolean) = {
    if (verbose) {
      log.info(s"Completed checks in [${System.currentTimeMillis - startMs}ms]")(TraceData.noop)
    }
    log.info("You're all good!")(TraceData.noop)
    ProjectileResponse.OK
  }
}
