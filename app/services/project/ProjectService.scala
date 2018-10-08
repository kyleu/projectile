package services.project

import io.scalaland.chimney.dsl._
import models.project.{Project, ProjectSummary}
import services.config.ConfigService
import util.JsonSerializers._

class ProjectService(val cfg: ConfigService) {
  private[this] val dir = cfg.projectDirectory
  private[this] val fn = "project.json"

  def list() = dir.children.toList.map(_.name.stripSuffix(".json")).sorted.map(getSummary)

  def getSummary(key: String) = {
    val f = dir / key / fn
    if (f.exists && f.isRegularFile && f.isReadable) {
      decodeJson[ProjectSummary](f.contentAsString) match {
        case Right(is) => is
        case Left(x) => ProjectSummary(key = key, title = key, description = s"Error loading project: ${x.getMessage}", status = Some("Error"))
      }
    } else {
      ProjectSummary(key = key, title = key, description = s"Cannot load [$fn] for input [$key]", status = Some("Error"))
    }
  }

  def load(key: String) = {
    val summ = getSummary(key)
    summ.into[Project].transform
  }

  def save(p: ProjectSummary) = {
    // TODO
    p
  }

  def remove(key: String) = {
    // TODO
    getSummary(key)
  }
}
