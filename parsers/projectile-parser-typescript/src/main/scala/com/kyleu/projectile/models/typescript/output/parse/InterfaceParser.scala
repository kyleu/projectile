package com.kyleu.projectile.models.typescript.output.parse

import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}
import com.kyleu.projectile.models.typescript.node.TypeScriptNode
import com.kyleu.projectile.models.typescript.node.TypeScriptNode.InterfaceDecl
import com.kyleu.projectile.models.typescript.output.{OutputHelper, TypeScriptOutput}

object InterfaceParser {
  def parse(ctx: ParseContext, out: TypeScriptOutput, node: InterfaceDecl) = {
    val cn = ExportHelper.toClassName(node.name)

    val file = ScalaFile(path = OutputPath.SharedSource, dir = ctx.pkg, key = ExportHelper.toClassName(node.name))

    file.addImport(Seq("scala", "scalajs"), "js")

    OutputHelper.printJsDoc(file, node.ctx)
    file.add("@js.native")
    file.add(s"trait $cn extends js.Object {", 1)
    node.members.foreach(m => printMember(file, m))
    file.add("}", -1)

    ctx -> out.withAdditional(file)
  }

  def printMember(file: ScalaFile, m: TypeScriptNode) = {
    OutputHelper.printJsDoc(file, m.ctx)
    file.add(s"// $m")
  }
}
