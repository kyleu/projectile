package services.project

import io.scalaland.chimney.dsl._
import models.command.ProjectileResponse
import models.project._
import models.project.member.ProjectMember
import services.config.ConfigService
import util.JsonSerializers._

class ProjectSummaryService(val cfg: ConfigService) {
  private[this] val dir = cfg.projectDirectory
  private[this] val fn = "project.json"

  def list() = dir.children.toList.map(_.name.stripSuffix(".json")).sorted.map(getSummary)

  def getSummary(key: String) = {
    val f = dir / key / fn
    if (f.exists && f.isRegularFile && f.isReadable) {
      decodeJson[ProjectSummary](f.contentAsString) match {
        case Right(is) => is.copy(key = key)
        case Left(x) => ProjectSummary(key = key, title = key, description = s"Error loading project: ${x.getMessage}", status = Some("Error"))
      }
    } else {
      ProjectSummary(key = key, title = key, description = s"Cannot load [$fn] for input [$key]", status = Some("Error"))
    }
  }

  def load(key: String) = getSummary(key).into[Project]
    .withFieldComputed(_.enums, _ => loadDir[ProjectMember](s"$key/enum"))
    .withFieldComputed(_.models, _ => loadDir[ProjectMember](s"$key/model"))
    .transform

  def add(p: ProjectSummary) = {
    // TODO save summary
    p.into[Project].transform
  }

  def remove(key: String) = {
    (dir / key).delete(swallowIOExceptions = true)
    ProjectileResponse.OK
  }

  private[this] def loadDir[A: Decoder](k: String) = {
    val d = dir / k
    if (d.exists && d.isDirectory && d.isReadable) {
      d.children.map(f => loadFile[A](f, k)).toList
    } else {
      Nil
    }
  }
}
