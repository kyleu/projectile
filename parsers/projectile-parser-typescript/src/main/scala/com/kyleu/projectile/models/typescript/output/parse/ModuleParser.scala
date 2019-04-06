package com.kyleu.projectile.models.typescript.output.parse

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}
import com.kyleu.projectile.models.typescript.node.TypeScriptNode
import com.kyleu.projectile.models.typescript.node.TypeScriptNode.ModuleDecl
import com.kyleu.projectile.models.typescript.output.OutputHelper

object ModuleParser {
  def parse(ctx: ParseContext, config: ExportConfiguration, node: ModuleDecl) = {
    val cn = ExportHelper.toClassName(node.name)

    val filterResult = MemberParser.filter(filter(node.statements))
    val filteredMembers = filterResult.members.filter {
      case x: TypeScriptNode.VariableDecl if x.typ.t.isInstanceOf[FieldType.ObjectType] => false
      case _ => true
    }

    val moduleFile = if (filteredMembers.isEmpty) {
      Nil
    } else {
      val file = ScalaFile(path = OutputPath.SharedSource, dir = config.mergedApplicationPackage(ctx.pkg), key = cn)
      OutputHelper.printContext(file, node.ctx)

      MemberHelper.addGlobal(file, config, ctx, None)
      file.add(s"object $cn extends js.Object {", 1)
      filteredMembers.foreach(m => MemberParser.print(ctx = ctx, config = config, tsn = m, file = file, last = filteredMembers.lastOption.contains(m)))
      file.add("}", -1)
      file.add()
      Seq(file)
    }

    filterResult.extraClasses.foldLeft(ctx -> config.withAdditional(moduleFile: _*)) { (carry, decl) =>
      ObjectTypeParser.parseLiteral(carry._1, carry._2, decl._1, decl._2, decl._3, decl._4)
    }
  }

  def filter(nodes: Seq[TypeScriptNode]) = nodes.filter {
    case _: TypeScriptNode.ClassDecl => false
    case _: TypeScriptNode.EnumDecl => false
    case _: TypeScriptNode.InterfaceDecl => false
    case _: TypeScriptNode.ModuleDecl => false
    case _: TypeScriptNode.ExportNamespaceDecl => false
    case _: TypeScriptNode.SourceFileReference => false
    case _ => true
  }
}
