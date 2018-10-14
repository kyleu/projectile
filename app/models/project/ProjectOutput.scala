package models.project

import models.project.feature.FeatureOutput

import util.JsonSerializers._

object ProjectOutput {
  implicit val jsonEncoder: Encoder[ProjectOutput] = deriveEncoder
  implicit val jsonDecoder: Decoder[ProjectOutput] = deriveDecoder
}

case class ProjectOutput(
    project: String,
    rootLogs: Seq[String],
    featureOutput: Seq[FeatureOutput],
    duration: Long
)
