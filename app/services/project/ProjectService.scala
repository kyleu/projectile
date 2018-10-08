package services.project

import models.project.ProjectSummary
import services.config.ConfigService
import util.JsonSerializers._

class ProjectService(val cfg: ConfigService) {
  private[this] val dir = cfg.projectDirectory
  private[this] val fn = "project.json"

  def list() = dir.children.toSeq.map(_.name.stripSuffix(".json")).sorted.map(getSummary)

  def getSummary(key: String) = {
    val f = dir / key / fn
    if (f.exists && f.isRegularFile && f.isReadable) {
      decodeJson[ProjectSummary](f.contentAsString) match {
        case Right(is) => is
        case Left(x) => throw x
      }
    } else {
      throw new IllegalStateException(s"Cannot load [$fn] for input [$key]")
    }
  }
}
