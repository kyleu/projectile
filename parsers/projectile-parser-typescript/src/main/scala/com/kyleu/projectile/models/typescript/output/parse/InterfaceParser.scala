package com.kyleu.projectile.models.typescript.output.parse

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}
import com.kyleu.projectile.models.typescript.node.TypeScriptNode.InterfaceDecl
import com.kyleu.projectile.models.typescript.output.OutputHelper

object InterfaceParser {
  def parse(ctx: ParseContext, config: ExportConfiguration, node: InterfaceDecl) = {
    val cn = ExportHelper.escapeKeyword(ExportHelper.toClassName(node.name))

    val file = ScalaFile(path = OutputPath.SharedSource, dir = config.applicationPackage ++ ctx.pkg, key = cn)

    file.addImport(Seq("scala", "scalajs"), "js")

    OutputHelper.printContext(file, node.ctx)
    file.add("@js.native")
    val tp = OutputHelper.tParams(node.tParams)
    if (node.members.isEmpty) {
      file.add(s"trait $cn$tp extends js.Object", 1)
    } else {
      file.add(s"trait $cn$tp extends js.Object {", 1)
      val (members, extraClasses) = MemberParser.filter(node.members)

      members.foreach(m => MemberParser.print(ctx = ctx, config = config, tsn = m, file = file, last = members.lastOption.contains(m)))
      file.add("}", -1)
    }

    ctx -> config.withAdditional(file)
  }
}
