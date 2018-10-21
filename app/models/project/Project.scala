package models.project

import java.time.LocalDateTime

import io.scalaland.chimney.dsl._
import models.output.{OutputPackage, OutputPath}
import models.output.feature.Feature
import models.project.member.ProjectMember
import util.DateUtils
import util.JsonSerializers._

object Project {
  implicit val jsonEncoder: Encoder[Project] = deriveEncoder
  implicit val jsonDecoder: Decoder[Project] = deriveDecoder
}

case class Project(
    template: ProjectTemplate = ProjectTemplate.Custom,
    key: String,
    title: String,
    description: String = "...",
    features: Set[Feature] = Set.empty,
    paths: Map[OutputPath, String] = Map.empty,
    packages: Map[OutputPackage, Seq[String]] = Map.empty,
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

  lazy val toSummary = this.into[ProjectSummary].transform
}
