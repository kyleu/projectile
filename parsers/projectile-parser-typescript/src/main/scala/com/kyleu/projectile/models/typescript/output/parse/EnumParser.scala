package com.kyleu.projectile.models.typescript.output.parse

import com.kyleu.projectile.models.export.ExportEnum
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.input.InputType.Enum.TypeScriptEnum
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}
import com.kyleu.projectile.models.typescript.node.TypeScriptNode
import com.kyleu.projectile.models.typescript.node.TypeScriptNode.EnumDecl
import com.kyleu.projectile.models.typescript.output.OutputHelper

object EnumParser {
  def load(ctx: ParseContext, out: ExportConfiguration, node: EnumDecl) = {
    val members = node.members.collect { case e: TypeScriptNode.EnumMember => e }
    val enumVals = members.map(m => ExportEnum.EnumVal(k = m.name, i = m.initial.flatMap(_.asNumber.flatMap(_.toInt)), s = m.initial.flatMap(_.asString)))
    val enum = ExportEnum(inputType = TypeScriptEnum, pkg = ctx.pkg, key = node.name, className = ExportHelper.toClassName(node.name), values = enumVals)
    ctx -> out.withEnums(enum)
  }

  def parse(ctx: ParseContext, out: ExportConfiguration, node: EnumDecl) = {
    val cn = ExportHelper.toClassName(node.name)

    val file = ScalaFile(path = OutputPath.SharedSource, dir = out.applicationPackage ++ ctx.pkg, key = ExportHelper.toClassName(node.name))

    OutputHelper.printContext(file, node.ctx)
    file.add("@js.native")
    file.add(s"sealed trait $cn extends js.Object")
    file.add()
    MemberHelper.addGlobal(file, out, ctx, Some(node.name))
    file.add(s"object $cn extends js.Object {", 1)

    val members = node.members.collect { case e: TypeScriptNode.EnumMember => e }
    members.foreach { e =>
      OutputHelper.printContext(file, e.ctx)
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
