package models.project

import java.time.LocalDateTime

import models.project.feature.ProjectFeature
import models.project.member.ProjectMember
import util.DateUtils

case class Project(
    template: ProjectTemplate = ProjectTemplate.Simple,
    key: String,
    title: String,
    description: String = "...",
    features: Set[ProjectFeature] = Set.empty,
    enums: Seq[ProjectMember] = Nil,
    models: Seq[ProjectMember] = Nil,
    status: Option[String] = None,
    created: LocalDateTime = DateUtils.now,
    updated: LocalDateTime = DateUtils.now
) extends Ordered[Project] {
  private[this] def notFound(t: String, k: String) = throw new IllegalStateException(s"No $t in project [$key] with key [$k]")

  def getEnum(enum: String) = enums.find(_.outputKey == enum).getOrElse(notFound("enum", enum))
  def getModel(model: String) = models.find(_.outputKey == model).getOrElse(notFound("model", model))

  def getMember(t: ProjectMember.OutputType, member: String) = t match {
    case ProjectMember.OutputType.Enum => getEnum(member)
    case ProjectMember.OutputType.Model => getModel(member)
  }

  val allMembers = enums ++ models

  override def compare(p: Project) = title.compare(p.title)
}
