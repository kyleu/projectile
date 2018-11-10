package models.output.file

import models.output.OutputPath

case class ThriftFile(
    override val dir: Seq[String], override val key: String
) extends OutputFile(path = OutputPath.ThriftOutput, dir = dir, key = key, filename = key + ".thrift") {
  override def prefix = "// Generated File\n"

  override protected val icon = models.template.Icons.web
}
