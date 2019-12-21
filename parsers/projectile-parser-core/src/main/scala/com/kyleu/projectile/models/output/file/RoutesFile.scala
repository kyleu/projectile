package com.kyleu.projectile.models.output.file

import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.template.Icons

final case class RoutesFile(override val key: String) extends OutputFile(path = OutputPath.ServerResource, dir = Nil, key = key, filename = key + ".routes") {
  override def prefix = "# Generated File\n"

  override protected val icon = Icons.web
}
