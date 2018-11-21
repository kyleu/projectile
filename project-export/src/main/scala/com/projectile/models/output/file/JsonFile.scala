package com.projectile.models.output.file

import com.projectile.models.output.OutputPath
import com.projectile.models.template.Icons

case class JsonFile(
    override val path: OutputPath, override val dir: Seq[String], override val key: String
) extends OutputFile(path = path, dir = dir, key = key, filename = key + ".json") {
  override def prefix = s"// Generated File\n"
  override protected val icon = Icons.json
}
