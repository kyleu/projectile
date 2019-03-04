package com.kyleu.projectile.models.typescript.output.parse

import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}
import com.kyleu.projectile.models.typescript.node.TypeScriptNode
import com.kyleu.projectile.models.typescript.node.TypeScriptNode.EnumDecl
import com.kyleu.projectile.models.typescript.output.{OutputHelper, TypeScriptOutput}

object EnumParser {
  def parse(ctx: ParseContext, out: TypeScriptOutput, node: EnumDecl) = {
    val cn = ExportHelper.toClassName(node.name)

    val file = ScalaFile(path = OutputPath.SharedSource, dir = ctx.pkg, key = ExportHelper.toClassName(node.name))

    file.addImport(Seq("scala", "scalajs"), "js")

    OutputHelper.printJsDoc(file, node.ctx)
    file.add("@js.native")
    file.add(s"sealed trait $cn extends js.Object")
    file.add()
    file.add("@js.native")
    file.add(s"""@js.annotation.JSGlobal("${OutputHelper.jsPkg(ctx.pkg :+ node.name).mkString(".")}")""")
    file.add(s"object $cn extends js.Object {", 1)
    val members = node.members.collect { case e: TypeScriptNode.EnumMember => e }
    members.foreach { e =>
      val comment = e.initial.map(i => s" // $i").getOrElse("")
      file.add(s"""val ${e.name}: ${ExportHelper.toClassName(node.name)} = js.native$comment""")
    }
    file.add()
    file.add("@js.annotation.JSBracketAccess")
    file.add(s"def apply(value: $cn): ${typOf(members)} = js.native")
    file.add("}", -1)

    ctx -> out.withAdditional(file)
  }

  private[this] def typOf(members: Seq[TypeScriptNode.EnumMember]) = members match {
    case x if x.forall(_.initial.forall(_.asString.isDefined)) => "String"
    case x if x.forall(_.initial.forall(_.asNumber.exists(_.toInt.isDefined))) => "Int"
    case x if x.forall(_.initial.forall(_.asNumber.exists(_.toLong.isDefined))) => "Long"
    case x if x.forall(_.initial.forall(_.asNumber.isDefined)) => "Double"
    case _ => "js.Any"
  }
}
