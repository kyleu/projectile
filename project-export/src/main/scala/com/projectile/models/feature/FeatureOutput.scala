package com.projectile.models.feature

import com.projectile.models.output.OutputLog
import com.projectile.models.output.file.{InjectResult, OutputFile}
import com.projectile.util.JsonSerializers._

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
