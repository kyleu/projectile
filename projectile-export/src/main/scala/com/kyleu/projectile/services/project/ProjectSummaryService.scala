package com.kyleu.projectile.services.project

import io.scalaland.chimney.dsl._
import com.kyleu.projectile.models.project._
import com.kyleu.projectile.services.config.ConfigService
import com.kyleu.projectile.util.JsonSerializers._

class ProjectSummaryService(val cfg: ConfigService) {
  private[this] val dir = cfg.projectDirectory
  private[this] val fn = "project.json"

  def immediateList() = if (dir.exists) {
    dir.children.filter(_.isDirectory).toList.map(_.name.stripSuffix(".json")).flatMap(getSummary)
  } else {
    Nil
  }

  def list(): Seq[ProjectSummary] = immediateList() ++ cfg.linkedConfigs.map(c => new ProjectSummaryService(c)).flatMap(_.list()).sortBy(_.key)

  def getSummary(key: String): Option[ProjectSummary] = {
    val d = cfg.configForProject(key).getOrElse(cfg).projectDirectory
    val f = d / key / fn
    if (f.exists && f.isRegularFile && f.isReadable) {
      decodeJson[ProjectSummary](f.contentAsString) match {
        case Right(is) => Some(is.copy(key = key))
        case Left(x) => throw x
      }
    } else {
      cfg.linkedConfigs.find(_.containsProject(key)) match {
        case Some(c) => new ProjectSummaryService(c).getSummary(key)
        case _ => throw new IllegalStateException(s"Cannot load input with key [$key]")
      }
    }
  }

  def add(p: ProjectSummary) = {
    val d = cfg.configForProject(p.key).getOrElse(cfg).projectDirectory
    val f = d / p.key / fn
    f.createFileIfNotExists(createParents = true)
    f.overwrite(printJson(p.asJson))
    p.into[Project].transform
  }
}
