package models.project

import java.time.LocalDateTime

import models.project.feature.ProjectFeature
import util.DateUtils
import util.JsonSerializers._

object ProjectSummary {
  implicit val jsonEncoder: Encoder[ProjectSummary] = deriveEncoder
  implicit val jsonDecoder: Decoder[ProjectSummary] = deriveDecoder
}

case class ProjectSummary(
    template: ProjectTemplate = ProjectTemplate.Simple,
    key: String = "new",
    title: String = "New Project",
    description: String = "...",
    features: Set[ProjectFeature] = Set.empty,
    status: Option[String] = None,
    created: LocalDateTime = DateUtils.now,
    updated: LocalDateTime = DateUtils.now
) extends Ordered[ProjectSummary] {
  override def compare(p: ProjectSummary) = title.compare(p.title)
}
