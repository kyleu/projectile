package models.output.feature

import models.output.OutputLog
import models.output.file.{InjectResult, OutputFile}
import util.JsonSerializers._

object FeatureOutput {
  implicit val jsonEncoder: Encoder[FeatureOutput] = deriveEncoder
  implicit val jsonDecoder: Decoder[FeatureOutput] = deriveDecoder
}

case class FeatureOutput(
    feature: ProjectFeature,
    files: Seq[OutputFile.Rendered],
    injections: Seq[InjectResult],
    logs: Seq[OutputLog],
    duration: Long
)
