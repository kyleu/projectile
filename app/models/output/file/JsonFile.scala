package models.output.file

import models.output.OutputPath

case class JsonFile(
    override val path: OutputPath, override val dir: Seq[String], override val key: String
) extends OutputFile(path = path, dir = dir, key = key, filename = key + ".json") {
  override def prefix = s"// Generated File\n"
  override protected val icon = models.template.Icons.json
}
