package com.kyleu.projectile.services.config

import com.kyleu.projectile.models.command.ProjectileResponse
import com.kyleu.projectile.util.tracing.TraceData
import com.kyleu.projectile.util.{JacksonUtils, Logging}

class ConfigService(val path: String) extends Logging {
  val workingDirectory = better.files.File.apply(path)
  if (!workingDirectory.exists) {
    log.warn(s"workingDirectory [${workingDirectory.pathAsString}] does not exist")(TraceData.noop)
  }

  val configDirectory = workingDirectory / ".projectile"

  val inputDirectory = configDirectory / "input"
  def containsInput(key: String): Boolean = (inputDirectory / key).exists
  def configForInput(key: String): Option[ConfigService] = if (containsInput(key)) {
    Some(this)
  } else {
    linkedConfigs.flatMap(_.configForInput(key)).headOption
  }

  val projectDirectory = configDirectory / "project"
  def containsProject(key: String): Boolean = (projectDirectory / key).exists
  def configForProject(key: String): Option[ConfigService] = if (containsProject(key)) {
    Some(this)
  } else {
    linkedConfigs.flatMap(_.configForProject(key)).headOption
  }

  def projectDir(key: String) = {
    val dir = projectDirectory / key
    if (dir.exists && dir.isDirectory && dir.isWriteable) {
      dir
    } else {
      throw new IllegalStateException(s"Cannot load project dir for [$key]")
    }
  }

  val available = inputDirectory.isDirectory && inputDirectory.isWriteable && projectDirectory.isDirectory && projectDirectory.isWriteable

  val linkedConfigs = {
    val f = configDirectory / "linked.json"
    if (f.exists && f.isReadable) {
      val dirs = JacksonUtils.extractString[Seq[String]](f.contentAsString)
      val configs = dirs.map(_.trim).filter(_.nonEmpty).filterNot(_ == ".").distinct.map { d =>
        val f = workingDirectory / d
        new ConfigService(f.pathAsString)
      }
      configs.foreach {
        case cfg if !cfg.available => log.warn(s"Linked configuration directory [${cfg.path}] is not initialized")(TraceData.noop)
        case _ => // noop
      }
      configs
    } else {
      Nil
    }
  }

  def init() = {
    inputDirectory.createDirectories()
    projectDirectory.createDirectories()
    ProjectileResponse.OK
  }
}
