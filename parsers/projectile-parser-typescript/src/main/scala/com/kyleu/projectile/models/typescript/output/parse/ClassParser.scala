package com.kyleu.projectile.models.typescript.output.parse

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.TypeParam
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}
import com.kyleu.projectile.models.typescript.node.TypeScriptNode.ClassDecl
import com.kyleu.projectile.models.typescript.node.{ModifierFlag, NodeContext, TypeScriptNode}
import com.kyleu.projectile.models.typescript.output.OutputHelper

object ClassParser {
  def parseClass(ctx: ParseContext, config: ExportConfiguration, node: ClassDecl) = {
    parse(ctx, config, node.name, node.tParams, node.ctx, node.members)
  }

  private[this] def parse(
    ctx: ParseContext,
    config: ExportConfiguration,
    name: String,
    tParams: Seq[TypeParam],
    nodeCtx: NodeContext,
    members: Seq[TypeScriptNode]
  ) = {
    val cn = ExportHelper.escapeKeyword(ExportHelper.toClassName(name))

    val file = ScalaFile(path = OutputPath.SharedSource, dir = ctx.pkg, key = cn)

    file.addImport(Seq("scala", "scalajs"), "js")

    OutputHelper.printContext(file, nodeCtx)

    val (staticMembers, instMembers) = members.filterNot(_.ctx.isPrivate).partition(_.ctx.modifiers(ModifierFlag.Static))

    if (staticMembers.nonEmpty) {
      file.add("@js.native")
      file.add("@js.annotation.JSGlobal")
      file.add(s"object $cn extends js.Object {", 1)
      staticMembers.foreach(m => MemberParser.print(ctx = ctx, config = config, tsn = m, file = file, last = staticMembers.lastOption.contains(m)))
      file.add("}", -1)
      file.add()
    }

    file.add("@js.native")
    file.add("@js.annotation.JSGlobal")
    val prefix = ModifierFlag.keywordFlags(nodeCtx.modifiers)

    val tp = OutputHelper.tParams(tParams)

    val constructors = members.collect { case n: TypeScriptNode.Constructor => n }
    val publicDefaultConstructor = constructors.find(c => c.params.isEmpty && c.ctx.isPublic)
    val hiddenConstructor = if (constructors.isEmpty || publicDefaultConstructor.isDefined) { "" } else { " protected ()" }
    val finalMembers = instMembers.filterNot(publicDefaultConstructor.contains)

    if (finalMembers.isEmpty) {
      file.add(s"${prefix}class $cn$tp$hiddenConstructor extends js.Object")
    } else {
      file.add(s"${prefix}class $cn$tp$hiddenConstructor extends js.Object {", 1)
      finalMembers.foreach(m => MemberParser.print(ctx = ctx, config = config, tsn = m, file = file, last = instMembers.lastOption.contains(m)))
      file.add("}", -1)
    }

    ctx -> config.withAdditional(file)
  }
}
