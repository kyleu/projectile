package com.kyleu.projectile.models.typescript.output

import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.typescript.node.{ModifierFlag, NodeContext}

object OutputHelper {
  def printContext(file: ScalaFile, ctx: NodeContext) = ctx.jsDoc.flatMap(_.commentSeq).toList match {
    case Nil => // noop
    case h :: Nil => file.add("/** " + h + " */")
    case x =>
      file.add("/**")
      x.foreach(l => file.add(" * " + l))
      file.add(" */")
  }

  def jsPkg(pkg: Seq[String]) = pkg.drop(1)

  def keywords(modifiers: Set[ModifierFlag]) = modifiers.filter(_.scalaKeyword).map(_ + " ").mkString
}
