package com.kyleu.projectile.models.typescript.output.parse

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}
import com.kyleu.projectile.models.typescript.node.TypeScriptNode.InterfaceDecl
import com.kyleu.projectile.models.typescript.output.OutputHelper

object InterfaceParser {
  def parse(ctx: ParseContext, config: ExportConfiguration, node: InterfaceDecl) = {
    val cn = ExportHelper.escapeKeyword(ExportHelper.toClassName(node.name))

    val file = ScalaFile(path = OutputPath.SharedSource, dir = config.mergedApplicationPackage(ctx.pkg), key = cn)
    file.addImport(Seq("scala", "scalajs"), "js")

    OutputHelper.printContext(file, node.ctx)
    val tp = OutputHelper.tParams(node.tParams)
    if (node.members.isEmpty) {
      file.add("@js.native")
      file.add(s"trait $cn$tp extends js.Object")
    } else {
      val filterResult = MemberParser.filter(node.members)
      val filteredMembers = filterResult.members

      OutputHelper.printObjectMembers(ctx = ctx, config = config, file = file, members = filteredMembers, objKey = Some(cn))

      file.add("@js.native")
      file.add(s"trait $cn$tp extends js.Object {", 1)
      filteredMembers.foreach(m => MemberParser.print(ctx = ctx, config = config, tsn = m, file = file, last = filteredMembers.lastOption.contains(m)))
      file.add("}", -1)
    }

    ctx -> config.withAdditional(file)
  }
}
