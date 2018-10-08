package models.project

import java.time.LocalDateTime

import util.DateUtils

case class Project(
    template: ProjectTemplate = ProjectTemplate.Simple,
    key: String,
    title: String,
    description: String = "...",
    status: Option[String] = None,
    created: LocalDateTime = DateUtils.now,
    updated: LocalDateTime = DateUtils.now
) extends Ordered[Project] {
  override def compare(p: Project) = title.compare(p.title)
}
