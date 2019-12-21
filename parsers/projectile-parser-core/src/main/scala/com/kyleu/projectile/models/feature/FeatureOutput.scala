package com.kyleu.projectile.models.feature

import com.kyleu.projectile.models.output.OutputLog
import com.kyleu.projectile.models.output.file.{InjectResult, OutputFile}
import com.kyleu.projectile.util.JsonSerializers._

object FeatureOutput {
  implicit val jsonEncoder: Encoder[FeatureOutput] = deriveEncoder
  implicit val jsonDecoder: Decoder[FeatureOutput] = deriveDecoder
}

final case class FeatureOutput(
    feature: ProjectFeature,
    files: Seq[OutputFile.Rendered],
    injections: Seq[InjectResult],
    logs: Seq[OutputLog],
    duration: Long
)
