package com.kyleu.projectile.models.typescript.output.parse

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}
import com.kyleu.projectile.models.typescript.node.{SyntaxKind, TypeScriptNode}
import com.kyleu.projectile.models.typescript.node.TypeScriptNode.ModuleDecl
import com.kyleu.projectile.models.typescript.output.OutputHelper

object ModuleParser {
  def parse(ctx: ParseContext, config: ExportConfiguration, node: ModuleDecl) = {
    val cn = ExportHelper.toClassName(node.name)

    val (members, extraClasses) = MemberParser.filter(filter(node.statements))

    val moduleFile = if (members.isEmpty) {
      Nil
    } else {
      val file = ScalaFile(path = OutputPath.SharedSource, dir = ctx.pkg, key = cn)
      file.addImport(Seq("scala", "scalajs"), "js")
      OutputHelper.printContext(file, node.ctx)

      file.add("@js.native")
      file.add(s"""@js.annotation.JSGlobal("${ctx.pkg.mkString(".")}")""")
      file.add(s"object $cn extends js.Object {", 1)
      members.foreach(m => MemberParser.print(ctx = ctx, config = config, tsn = m, file = file, last = members.lastOption.contains(m)))
      file.add("}", -1)
      file.add()
      Seq(file)
    }

    extraClasses.foldLeft(ctx -> config.withAdditional(moduleFile: _*)) { (carry, decl) =>
      ObjectTypeParser.parseLiteral(carry._1, carry._2, decl.name, decl.typ.t.asInstanceOf[FieldType.ObjectType], decl.typ.r, decl.ctx)
    }
  }

  def filter(nodes: Seq[TypeScriptNode]) = nodes.filter {
    case _: TypeScriptNode.ClassDecl => false
    case _: TypeScriptNode.EnumDecl => false
    case _: TypeScriptNode.InterfaceDecl => false
    case _: TypeScriptNode.ModuleDecl => false
    case x: TypeScriptNode.VariableDecl if x.typ.t.isInstanceOf[FieldType.ObjectType] => false
    case _ => true
  }
}
