package com.kyleu.projectile.models.typescript.output

import com.kyleu.projectile.models.export.typ.TypeParam
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.typescript.node.{NodeContext, NodeHelper, TypeScriptNode}

object OutputHelper {
  def printContext(file: ScalaFile, ctx: NodeContext) = ctx.jsDoc match {
    case None => // noop
    case Some(jsDoc) =>
      jsDoc.comment match {
        case h :: Nil if jsDoc.params.isEmpty => file.add("/** " + h + " */")
        case _ =>
          file.add("/**")
          jsDoc.comment.foreach(c => file.add(" * " + c))
          jsDoc.examples.filterNot(_.isEmpty).foreach { ex =>
            file.add(" * ")
            file.add(" * {{{")
            ex.foreach(l => file.add(" * " + l))
            file.add(" * }}}")
          }
          if (jsDoc.params.nonEmpty) {
            file.add(" * ")
            jsDoc.params.foreach { p =>
              file.add(s" * @param ${p.name}${p.comment.headOption.map(" " + _).getOrElse("")}")
              if (p.comment.nonEmpty) {
                p.comment.tail.foreach(c => file.add(" * " + (0 until (p.name.length + 8)).map(_ => " ").mkString + c))
              }
            }
          }
          if (jsDoc.ret.nonEmpty) {
            file.add(" * ")
            jsDoc.ret.foreach { r =>
              file.add(s" * @return $r")
            }
          }
          file.add(" */")
          jsDoc.deprecated.foreach(r => file.add(s"""@deprecated("$r", "???")"""))
      }
  }

  def keywords(node: TypeScriptNode) = node.ctx.modifiers.map(_ + " ").mkString

  def tParams(tParams: Seq[TypeParam]) = tParams.toList match {
    case Nil => ""
    case l => s"[${l.map(p => p.name + p.constraint.map(c => s" <: " + NodeHelper.pt(c)).getOrElse("")).mkString(", ")}]"
  }
}
