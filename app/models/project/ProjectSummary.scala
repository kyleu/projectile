package models.project

import java.time.LocalDateTime

import models.project.template.ProjectTemplate
import util.DateUtils
import util.JsonSerializers._

object ProjectSummary {
  implicit val jsonEncoder: Encoder[ProjectSummary] = deriveEncoder
  implicit val jsonDecoder: Decoder[ProjectSummary] = deriveDecoder
}

case class ProjectSummary(
    template: ProjectTemplate = ProjectTemplate.Simple,
    key: String,
    title: String,
    description: String = "...",
    status: Option[String] = None,
    created: LocalDateTime = DateUtils.now,
    updated: LocalDateTime = DateUtils.now
) extends Ordered[ProjectSummary] {
  override def compare(p: ProjectSummary) = title.compare(p.title)
}
