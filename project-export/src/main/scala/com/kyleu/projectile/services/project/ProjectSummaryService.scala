package com.kyleu.projectile.services.project

import io.scalaland.chimney.dsl._
import com.kyleu.projectile.models.project._
import com.kyleu.projectile.services.config.ConfigService
import com.kyleu.projectile.util.JsonSerializers._

class ProjectSummaryService(val cfg: ConfigService) {
  private[this] val dir = cfg.projectDirectory
  private[this] val fn = "project.json"

  def list() = dir.children.toList.map(_.name.stripSuffix(".json")).sorted.flatMap(getSummary)

  def getSummary(key: String) = {
    val f = dir / key / fn
    if (f.exists && f.isRegularFile && f.isReadable) {
      decodeJson[ProjectSummary](f.contentAsString) match {
        case Right(is) => Some(is.copy(key = key))
        case Left(x) =>
          // Some(ProjectSummary(key = key, title = key, description = s"Error loading project: ${x.getMessage}", status = Some("Error")))
          throw x
      }
    } else {
      // Some(ProjectSummary(key = key, title = key, description = s"Cannot load [$fn] for input [$key]", status = Some("Error")))
      None
    }
  }

  def add(p: ProjectSummary) = {
    val f = dir / p.key / fn
    f.createFileIfNotExists(createParents = true)
    f.overwrite(printJson(p.asJson))
    p.into[Project].transform
  }
}
