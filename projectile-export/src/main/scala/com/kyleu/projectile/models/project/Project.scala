package com.kyleu.projectile.models.project

import io.scalaland.chimney.dsl._
import com.kyleu.projectile.models.output.{OutputPackage, OutputPath}
import com.kyleu.projectile.models.feature.{EnumFeature, ModelFeature, ProjectFeature, ServiceFeature}
import com.kyleu.projectile.models.input.InputTemplate
import com.kyleu.projectile.models.project.member.{EnumMember, ModelMember, ServiceMember, UnionMember}
import com.kyleu.projectile.util.JsonSerializers._

object Project {
  implicit val jsonEncoder: Encoder[Project] = deriveEncoder
  implicit val jsonDecoder: Decoder[Project] = deriveDecoder
}

case class Project(
    template: ProjectTemplate = ProjectTemplate.Custom,
    key: String,
    input: String,
    description: String = "...",
    features: Set[ProjectFeature] = Set(ProjectFeature.Core),
    paths: Map[OutputPath, String] = Map.empty,
    packages: Map[OutputPackage, Seq[String]] = Map.empty,
    classOverrides: Map[String, String] = Map.empty,
    enums: Seq[EnumMember] = Nil,
    models: Seq[ModelMember] = Nil,
    unions: Seq[UnionMember] = Nil,
    services: Seq[ServiceMember] = Nil,
    defaultEnumFeatures: Set[String] = Set("core"),
    defaultModelFeatures: Set[String] = Set("core", "json"),
    defaultServiceFeatures: Set[String] = Set("core")
) extends Ordered[Project] {
  private[this] def notFound(t: String, k: String, candidates: => Seq[String]) = {
    throw new IllegalStateException(s"No $t in project [$key] with key [$k] among candidates [${candidates.mkString(", ")}]")
  }

  def availableFeatures(inputTemplate: InputTemplate) = ProjectFeature.values.filter(_.appliesTo(inputTemplate))

  def getPath(p: OutputPath): String = p match {
    case OutputPath.Root => paths.getOrElse(p, template.path(p))
    case _ =>
      val r = getPath(OutputPath.Root) match {
        case x if x.endsWith("/") => x
        case x => x + "/"
      }
      r + paths.getOrElse(p, template.path(p))
  }

  def getPackage(p: OutputPackage) = packages.getOrElse(p, p.defaultVal)

  def getEnumOpt(enum: String) = enums.find(_.key == enum)
  def getEnum(enum: String) = getEnumOpt(enum).getOrElse(notFound("enum", enum, enums.map(_.key)))
  def enumFeatures = EnumFeature.values.filter(e => e.dependsOn.forall(features.apply))

  def getModelOpt(model: String) = models.find(_.key == model)
  def getModel(model: String) = getModelOpt(model).getOrElse(notFound("model", model, models.map(_.key)))
  def modelFeatures = ModelFeature.values.filter(e => e.dependsOn.forall(features.apply))

  def getUnionOpt(union: String) = unions.find(_.key == union)
  def getUnion(union: String) = getUnionOpt(union).getOrElse(notFound("union", union, unions.map(_.key)))

  def getServiceOpt(svc: String) = services.find(_.key == svc)
  def getService(svc: String) = getServiceOpt(svc).getOrElse(notFound("service", svc, services.map(_.key)))
  def serviceFeatures = ServiceFeature.values.filter(e => e.dependsOn.forall(features.apply))

  override def compare(p: Project) = key.compare(p.key)

  lazy val toSummary = this.into[ProjectSummary].transform

  val pathset = features.flatMap(_.paths)
  val classOverrideStrings = classOverrides.toSeq.sortBy(_._1).map(o => s"${o._1} = ${o._2}")

  override def toString = s"$key - [${enums.size}] enums, [${models.size}] models, and [${services.size}] services"
}
