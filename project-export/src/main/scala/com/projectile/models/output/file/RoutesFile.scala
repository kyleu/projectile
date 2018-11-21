package com.projectile.models.output.file

import com.projectile.models.output.OutputPath
import com.projectile.models.template.Icons

case class RoutesFile(override val key: String) extends OutputFile(path = OutputPath.ServerResource, dir = Nil, key = key, filename = key + ".routes") {
  override def prefix = "# Generated File\n"

  override protected val icon = Icons.web
}
