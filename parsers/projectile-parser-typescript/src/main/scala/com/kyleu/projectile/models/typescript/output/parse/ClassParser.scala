package com.kyleu.projectile.models.typescript.output.parse

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.TypeParam
import com.kyleu.projectile.models.input.InputType
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}
import com.kyleu.projectile.models.typescript.node.TypeScriptNode.ClassDecl
import com.kyleu.projectile.models.typescript.node.{ModifierFlag, NodeContext, TypeScriptNode}
import com.kyleu.projectile.models.typescript.output.OutputHelper

object ClassParser {
  def load(ctx: ParseContext, out: ExportConfiguration, node: ClassDecl) = {
    val title = ExportHelper.toClassName(node.name)
    val model = ExportModel(
      inputType = InputType.Model.TypeScriptModel,
      key = node.name,
      pkg = ctx.pkg,
      propertyName = ExportHelper.toIdentifier(node.name),
      className = title,
      title = title,
      description = None,
      plural = ExportHelper.toDefaultPlural(title),
      fields = Nil
    )
    ctx -> out.withModels(model)
  }

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

    val file = ScalaFile(path = OutputPath.SharedSource, dir = config.mergedApplicationPackage(ctx.pkg), key = cn)

    OutputHelper.printContext(file, nodeCtx)

    val (staticMembers, instMembers) = members.filterNot(_.ctx.isPrivate).partition(_.ctx.modifiers(ModifierFlag.Static))

    if (staticMembers.nonEmpty) {
      MemberHelper.addGlobal(file, config, ctx, Some(name))
      file.add(s"object $cn extends js.Object {", 1)
      staticMembers.foreach(m => MemberParser.print(ctx = ctx, config = config, tsn = m, file = file, last = staticMembers.lastOption.contains(m)))
      file.add("}", -1)
      file.add()
    }

    val prefix = ModifierFlag.keywordFlags(nodeCtx.modifiers)

    val tp = OutputHelper.tParams(tParams)

    val constructors = members.collect { case n: TypeScriptNode.Constructor => n }
    val publicDefaultConstructor = constructors.find(c => c.params.isEmpty && c.ctx.isPublic)
    val hiddenConstructor = if (constructors.isEmpty || publicDefaultConstructor.isDefined) { "" } else { " protected()" }
    val finalMembers = instMembers.filterNot(publicDefaultConstructor.contains)

    MemberHelper.addGlobal(file, config, ctx, Some(name))
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
