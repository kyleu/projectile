package models.project

import models.output.feature.FeatureOutput
import util.JsonSerializers._

object ProjectOutput {
  implicit val jsonEncoder: Encoder[ProjectOutput] = deriveEncoder
  implicit val jsonDecoder: Decoder[ProjectOutput] = deriveDecoder
}

case class ProjectOutput(
    project: ProjectSummary,
    rootLogs: Seq[String],
    featureOutput: Seq[FeatureOutput],
    duration: Long
) {
  lazy val fileCount = featureOutput.map(_.files.size).sum

}
