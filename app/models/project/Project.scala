package models.project

import java.time.LocalDateTime

import models.project.feature.ProjectFeature
import util.DateUtils

case class Project(
    template: ProjectTemplate = ProjectTemplate.Simple,
    key: String,
    title: String,
    description: String = "...",
    features: Seq[ProjectFeature] = Nil,
    status: Option[String] = None,
    created: LocalDateTime = DateUtils.now,
    updated: LocalDateTime = DateUtils.now
) extends Ordered[Project] {
  override def compare(p: Project) = title.compare(p.title)
}
