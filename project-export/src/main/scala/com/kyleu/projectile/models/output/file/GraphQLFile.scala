package com.kyleu.projectile.models.output.file

import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.template.Icons

case class GraphQLFile(override val dir: Seq[String], override val key: String) extends OutputFile(
  path = OutputPath.GraphQLOutput, dir = dir, key = key, filename = key + ".graphql"
) {

  override def prefix = "# Generated File\n"

  override protected def icon = Icons.result
}
