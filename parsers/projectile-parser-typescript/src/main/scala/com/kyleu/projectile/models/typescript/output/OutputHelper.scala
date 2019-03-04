package com.kyleu.projectile.models.typescript.output

import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.typescript.node.NodeContext

object OutputHelper {
  def jsPkg(pkg: Seq[String]) = pkg.drop(1)

  def printJsDoc(file: ScalaFile, ctx: NodeContext) = ctx.jsDoc.flatMap(_.commentSeq).toList match {
    case Nil => // noop
    case h :: Nil => file.add("/** " + h + " */")
    case x =>
      file.add("/**")
      x.foreach(l => file.add(" * " + l))
      file.add(" */")
  }
}
