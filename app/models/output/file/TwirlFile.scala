package models.output.file

import models.output.OutputPath

case class TwirlFile(
    override val dir: Seq[String], override val key: String
) extends OutputFile(OutputPath.ServerSource, dir, key, key + ".scala.html") {
  override def prefix = "@* Generated File *@\n"

  override protected val icon = models.template.Icons.web
}
