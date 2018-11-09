package models.output.file

import models.output.OutputPath

case class RoutesFile(override val key: String) extends OutputFile(path = OutputPath.Root, dir = Seq("conf"), key = key, filename = key + ".routes") {
  override def prefix = "# Generated File\n"

  override protected val icon = models.template.Icons.web
}
