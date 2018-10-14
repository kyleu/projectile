package models.project.feature

import models.output.file.OutputFile

import util.JsonSerializers._

object FeatureOutput {
  implicit val jsonEncoder: Encoder[FeatureOutput] = deriveEncoder
  implicit val jsonDecoder: Decoder[FeatureOutput] = deriveDecoder
}

case class FeatureOutput(
    feature: ProjectFeature,
    files: Seq[OutputFile.Rendered],
    logs: Seq[String],
    duration: Long
)
