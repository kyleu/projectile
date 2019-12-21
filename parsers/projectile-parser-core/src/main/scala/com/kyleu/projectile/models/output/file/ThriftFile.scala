package com.kyleu.projectile.models.output.file

import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.template.Icons

final case class ThriftFile(
    override val dir: Seq[String], override val key: String
) extends OutputFile(path = OutputPath.ThriftOutput, dir = dir, key = key, filename = key + ".thrift") {
  override def prefix = "// Generated File\n"

  override protected val icon = Icons.web
}
