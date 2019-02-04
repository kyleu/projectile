package com.kyleu.projectile.models.output.file

import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.template.Icons

case class JsonFile(
    override val path: OutputPath, override val dir: Seq[String], override val key: String
) extends OutputFile(path = path, dir = dir, key = key, filename = key + ".json") {
  override def prefix = "// Generated File\n"
  override protected val icon = Icons.json
}
