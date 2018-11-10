package models.project

import better.files._
import models.output.{OutputLog, OutputPath}
import models.output.feature.FeatureOutput
import util.JsonSerializers._

object ProjectOutput {
  implicit val jsonEncoder: Encoder[ProjectOutput] = deriveEncoder
  implicit val jsonDecoder: Decoder[ProjectOutput] = deriveDecoder
}

case class ProjectOutput(
    project: ProjectSummary,
    rootLogs: Seq[OutputLog],
    featureOutput: Seq[FeatureOutput],
    duration: Long
) {
  def getDirectory(projectRoot: File, path: OutputPath): File = path match {
    case OutputPath.Root => projectRoot / project.paths.getOrElse(path, project.template.path(path))
    case _ => getDirectory(projectRoot, OutputPath.Root) / project.paths.getOrElse(path, project.template.path(path))
  }

  lazy val files = featureOutput.flatMap(_.files)
  lazy val fileCount = featureOutput.map(_.files.size).sum
}
