package services.config

import better.files.File
import util.Logging

object ConfigValidator extends Logging {
  def validate(svc: ConfigService, verbose: Boolean) = {
    val startMs = System.currentTimeMillis

    if (verbose) {
      log.info("Checking configuration and verifying files...")
    }

    checkDir(svc.workingDirectory, "Working").orElse(checkDir(svc.configDirectory, "Config")) match {
      case Some(err) => log.error(err)
      case None => checkDir(svc.projectDirectory, "Project").orElse(checkDir(svc.inputDirectory, "Input")) match {
        case Some(err) => log.error(err)
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

  private[this] def alrightyThen(startMs: Long, verbose: Boolean) = {
    if (verbose) {
      log.info(s"Completed checks in [${System.currentTimeMillis - startMs}ms]")
    }
    log.info("You're all good!")
  }
}
