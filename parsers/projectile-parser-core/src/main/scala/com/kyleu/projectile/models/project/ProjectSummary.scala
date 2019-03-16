package com.kyleu.projectile.models.project

import com.kyleu.projectile.models.output.{OutputPackage, OutputPath}
import com.kyleu.projectile.models.feature.{EnumFeature, ModelFeature, ProjectFeature, ServiceFeature}
import com.kyleu.projectile.util.JsonSerializers._

object ProjectSummary {
  implicit val jsonEncoder: Encoder[ProjectSummary] = deriveEncoder
  implicit val jsonDecoder: Decoder[ProjectSummary] = deriveDecoder

  def newObj(key: String) = ProjectSummary(key = key, features = Set(ProjectFeature.Core, ProjectFeature.DataModel))
}

case class ProjectSummary(
    key: String,
    template: ProjectTemplate = ProjectTemplate.Custom,
    input: String = "invalid",
    description: String = "...",
    features: Set[ProjectFeature] = Set(ProjectFeature.Core),
    paths: Map[OutputPath, String] = Map.empty,
    packages: Map[OutputPackage, Seq[String]] = Map.empty,
    classOverrides: Map[String, String] = Map.empty,
    defaultEnumFeatures: Set[String] = Set("core"),
    defaultModelFeatures: Set[String] = Set("core", "json"),
    defaultServiceFeatures: Set[String] = Set("core")
) extends Ordered[ProjectSummary] {
  override def compare(p: ProjectSummary) = key.compare(p.key)

  def enumFeatures = EnumFeature.values.filter(e => e.dependsOn.forall(features.apply))
  def modelFeatures = ModelFeature.values.filter(e => e.dependsOn.forall(features.apply))
  def serviceFeatures = ServiceFeature.values.filter(e => e.dependsOn.forall(features.apply))
}
