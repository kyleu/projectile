package com.projectile.models.project

import java.time.LocalDateTime

import io.scalaland.chimney.dsl._
import com.projectile.models.output.{OutputPackage, OutputPath}
import com.projectile.models.output.feature.{EnumFeature, ModelFeature, ProjectFeature}
import com.projectile.models.project.member.{EnumMember, ModelMember}
import com.projectile.util.DateUtils
import com.projectile.util.JsonSerializers._

object Project {
  implicit val jsonEncoder: Encoder[Project] = deriveEncoder
  implicit val jsonDecoder: Decoder[Project] = deriveDecoder
}

case class Project(
    template: ProjectTemplate = ProjectTemplate.Custom,
    key: String,
    title: String,
    description: String = "...",
    features: Set[ProjectFeature] = Set.empty,
    paths: Map[OutputPath, String] = Map.empty,
    packages: Map[OutputPackage, Seq[String]] = Map.empty,
    classOverrides: Map[String, String] = Map.empty,
    enums: Seq[EnumMember] = Nil,
    models: Seq[ModelMember] = Nil,
    status: Option[String] = None,
    created: LocalDateTime = DateUtils.now,
    updated: LocalDateTime = DateUtils.now
) extends Ordered[Project] {
  private[this] def notFound(t: String, k: String) = throw new IllegalStateException(s"No $t in project [$key] with key [$k]")

  def getPath(p: OutputPath): String = p match {
    case OutputPath.Root => paths.getOrElse(p, template.path(p))
    case _ => getPath(OutputPath.Root) + paths.getOrElse(p, template.path(p))
  }

  def getPackage(p: OutputPackage) = packages.getOrElse(p, p.defaultVal)

  def getEnumOpt(enum: String) = enums.find(_.key == enum)
  def getEnum(enum: String) = getEnumOpt(enum).getOrElse(notFound("enum", enum))

  def getModelOpt(model: String) = models.find(_.key == model)
  def getModel(model: String) = getModelOpt(model).getOrElse(notFound("model", model))

  def enumFeatures = EnumFeature.values.filter(e => e.dependsOn.forall(features.apply))
  def modelFeatures = ModelFeature.values.filter(e => e.dependsOn.forall(features.apply))

  override def compare(p: Project) = title.compare(p.title)

  lazy val toSummary = this.into[ProjectSummary].transform

  val pathset = features.flatMap(_.paths)
  val classOverrideStrings = classOverrides.toSeq.sortBy(_._1).map(x => s"${x._1} = ${x._2}")
}
