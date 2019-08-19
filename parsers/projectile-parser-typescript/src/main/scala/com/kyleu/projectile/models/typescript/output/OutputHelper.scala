package com.kyleu.projectile.models.typescript.output

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.{FieldType, FieldTypeRequired, ObjectField, TypeParam}
import com.kyleu.projectile.models.output.ExportHelper
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.typescript.node.{NodeContext, NodeHelper, TypeScriptNode}
import com.kyleu.projectile.models.typescript.output.parse.{MemberParser, ParseContext}

object OutputHelper {
  def printContext(file: ScalaFile, ctx: NodeContext) = ctx.jsDoc.foreach {
    case jsDoc if jsDoc.isEmpty => // noop
    case jsDoc if jsDoc.singleLine => file.add(s"/** ${jsDoc.comment.headOption.getOrElse("???")} */")
    case jsDoc =>
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
      jsDoc.see.foreach(s => file.add(s" * @see $s"))
      jsDoc.since.foreach(s => file.add(s" * @since $s"))
      jsDoc.deprecated.toList match {
        case Nil => // noop
        case _ :: t => t.dropWhile(_.trim.isEmpty).foreach(l => file.add(s""" * $l"""))
      }
      file.add(" */")
      jsDoc.deprecated.headOption.foreach(h => file.add(s"""@deprecated("$h", "0")"""))
  }

  def keywords(node: TypeScriptNode) = node.ctx.modifiers.map(_.toString + " ").mkString

  def tParams(tParams: Seq[TypeParam]) = tParams.toList match {
    case Nil => ""
    case l => s"[${l.map(p => p.name + p.constraint.map(c => s" <: " + NodeHelper.pt(c)).getOrElse("")).mkString(", ")}]"
  }

  def printObjectMembers(ctx: ParseContext, config: ExportConfiguration, file: ScalaFile, members: Seq[TypeScriptNode], objKey: Option[String]) = {
    val objs = members.flatMap(m => m.types.collect {
      case o: FieldType.ObjectType => m.nameOpt.getOrElse(o.key) -> o
    })
    if (objs.nonEmpty) {
      objKey.foreach(k => file.add(s"object ${ExportHelper.toClassName(k)} {", 1))
      printObjects(ctx = ctx, config = config, file = file, objs = objs)
      objKey.foreach(_ => file.add("}", -1))
      file.add()
    }
  }

  private[this] def printObjects(
    ctx: ParseContext, config: ExportConfiguration, file: ScalaFile, objs: Seq[(String, FieldType.ObjectType)], nested: Seq[String] = Nil
  ): Unit = objs.foreach { obj =>
    val kids = obj._2.fields.collect {
      case ObjectField(k, kid: FieldType.ObjectType, _, _) => k -> kid
    }
    val cn = ExportHelper.toClassName(obj._1)
    if (kids.nonEmpty) {
      file.add(s"object $cn {", 1)
      printObjects(ctx = ctx, config = config, file = file, objs = kids, nested = nested :+ cn)
      file.add("}", -1)
      file.add()
    }
    if (obj._2.fields.isEmpty) {
      file.add(s"trait $cn")
    } else {
      file.add(s"trait $cn {", 1)
      obj._2.fields.foreach { f =>
        val node = TypeScriptNode.VariableDecl(name = f.k, typ = FieldTypeRequired(f.t, f.req), ctx = NodeContext.empty)
        MemberParser.print(ctx = ctx, config = config, tsn = node, file = file, last = obj._2.fields.lastOption.contains(f))
      }
      file.add("}", -1)
    }
  }
}
