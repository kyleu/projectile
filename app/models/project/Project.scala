package models.project

import java.time.LocalDateTime

import models.project.feature.ProjectFeature
import util.DateUtils

case class Project(
    template: ProjectTemplate = ProjectTemplate.Simple,
    key: String,
    title: String,
    description: String = "...",
    features: Set[ProjectFeature] = Set.empty,
    enums: Seq[ProjectEnum] = Nil,
    models: Seq[ProjectModel] = Nil,
    services: Seq[ProjectSvc] = Nil,
    status: Option[String] = None,
    created: LocalDateTime = DateUtils.now,
    updated: LocalDateTime = DateUtils.now
) extends Ordered[Project] {
  private[this] def notFound(t: String, k: String) = throw new IllegalStateException(s"No $t in project [$key] with key [$k]")
  def getEnum(enum: String) = enums.find(_.source == enum).getOrElse(notFound("enum", enum))
  def getModel(model: String) = models.find(_.source == model).getOrElse(notFound("model", model))
  def getService(service: String) = services.find(_.source == service).getOrElse(notFound("service", service))

  override def compare(p: Project) = title.compare(p.title)
}
