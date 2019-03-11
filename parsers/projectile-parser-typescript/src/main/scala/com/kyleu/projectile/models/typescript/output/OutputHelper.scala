package com.kyleu.projectile.models.typescript.output

import com.kyleu.projectile.models.export.typ.TypeParam
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.typescript.node.{NodeContext, NodeHelper, TypeScriptNode}

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

  def keywords(node: TypeScriptNode) = node.ctx.modifiers.map(_ + " ").mkString

  def tParams(tParams: Seq[TypeParam]) = tParams.toList match {
    case Nil => ""
    case l => s"[${l.map(p => p.name + p.constraint.map(c => s" <: " + NodeHelper.pt(c)).getOrElse("")).mkString(", ")}]"
  }
}
