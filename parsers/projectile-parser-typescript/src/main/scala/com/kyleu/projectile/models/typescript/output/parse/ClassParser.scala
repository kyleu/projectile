package com.kyleu.projectile.models.typescript.output.parse

import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}
import com.kyleu.projectile.models.typescript.node.TypeScriptNode.ClassDecl
import com.kyleu.projectile.models.typescript.output.{OutputHelper, TypeScriptOutput}

object ClassParser {
  def parse(ctx: ParseContext, out: TypeScriptOutput, node: ClassDecl) = {
    val cn = ExportHelper.toClassName(node.name)

    val file = ScalaFile(path = OutputPath.SharedSource, dir = ctx.pkg, key = ExportHelper.toClassName(node.name))

    file.addImport(Seq("scala", "scalajs"), "js")

    OutputHelper.printContext(file, node.ctx)
    file.add("@js.native")
    file.add("@js.annotation.JSGlobal")
    val prefix = OutputHelper.keywords(node.ctx.modifiers)
    file.add(s"${prefix}class $cn extends js.Object {", 1)

    node.members.foreach(m => MemberParser.print(ctx = ctx, out = out, tsn = m, file = file, last = node.members.lastOption.contains(m)))

    file.add("}", -1)

    ctx -> out.withAdditional(file)
  }
}
