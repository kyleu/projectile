package com.projectile.models.project

import java.time.LocalDateTime

import com.projectile.models.output.{OutputPackage, OutputPath}
import com.projectile.models.feature.{EnumFeature, ModelFeature, ProjectFeature, ServiceFeature}
import com.projectile.util.DateUtils
import com.projectile.util.JsonSerializers._

object ProjectSummary {
  implicit val jsonEncoder: Encoder[ProjectSummary] = deriveEncoder
  implicit val jsonDecoder: Decoder[ProjectSummary] = deriveDecoder
}

case class ProjectSummary(
    template: ProjectTemplate = ProjectTemplate.Custom,
    key: String = "new",
    input: String = "FIX_SOURCE_INPUT",
    title: String = "New Project",
    description: String = "...",
    features: Set[ProjectFeature] = Set.empty,
    paths: Map[OutputPath, String] = Map.empty,
    packages: Map[OutputPackage, Seq[String]] = Map.empty,
    classOverrides: Map[String, String] = Map.empty,
    status: Option[String] = None,
    created: LocalDateTime = DateUtils.now,
    updated: LocalDateTime = DateUtils.now
) extends Ordered[ProjectSummary] {
  override def compare(p: ProjectSummary) = title.compare(p.title)

  def enumFeatures = EnumFeature.values.filter(e => e.dependsOn.forall(features.apply))
  def modelFeatures = ModelFeature.values.filter(e => e.dependsOn.forall(features.apply))
  def serviceFeatures = ServiceFeature.values.filter(e => e.dependsOn.forall(features.apply))
}
