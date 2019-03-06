package com.kyleu.projectile.models.typescript.output.parse

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}
import com.kyleu.projectile.models.typescript.node.TypeScriptNode.InterfaceDecl
import com.kyleu.projectile.models.typescript.output.OutputHelper

object InterfaceParser {
  def parse(ctx: ParseContext, config: ExportConfiguration, node: InterfaceDecl) = {
    val cn = ExportHelper.toClassName(node.name)

    val file = ScalaFile(path = OutputPath.SharedSource, dir = ctx.pkg, key = ExportHelper.toClassName(node.name))

    file.addImport(Seq("scala", "scalajs"), "js")

    OutputHelper.printContext(file, node.ctx)
    file.add("@js.native")
    file.add(s"trait $cn extends js.Object {", 1)

    node.members.foreach(m => MemberParser.print(ctx = ctx, config = config, tsn = m, file = file, last = node.members.lastOption.contains(m)))

    file.add("}", -1)

    ctx -> config.withAdditional(file)
  }
}
