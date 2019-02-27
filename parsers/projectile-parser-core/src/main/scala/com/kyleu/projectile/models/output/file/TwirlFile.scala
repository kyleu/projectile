package com.kyleu.projectile.models.output.file

import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.template.Icons

case class TwirlFile(
    override val dir: Seq[String], override val key: String
) extends OutputFile(OutputPath.ServerSource, dir, key, key + ".scala.html") {
  override def prefix = "@* Generated File *@\n"

  override protected val icon = Icons.web
}
