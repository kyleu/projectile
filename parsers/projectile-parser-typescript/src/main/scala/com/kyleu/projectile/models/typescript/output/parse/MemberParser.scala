package com.kyleu.projectile.models.typescript.output.parse

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ._
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.typescript.node.{NodeHelper, SyntaxKind, TypeScriptNode}
import com.kyleu.projectile.models.typescript.output.OutputHelper

object MemberParser {
  def filter(tsns: Seq[TypeScriptNode]): (Seq[TypeScriptNode], Seq[TypeScriptNode.VariableDecl]) = {
    val members = tsns.filterNot(_.ctx.isPrivate)
    def find(n: String) = members.find {
      case node: TypeScriptNode.MethodDecl if node.name == n => true
      case _ => false
    }

    val ambient = members.filter(x => x.ctx.kind == SyntaxKind.ExportAssignment || x.ctx.kind == SyntaxKind.DeclareKeyword)
    val ret = if (ambient.isEmpty) {
      members
    } else {
      ambient.map {
        case a: TypeScriptNode.ExportAssignment => find(a.exp) match {
          case Some(x) => x
          case None => TypeScriptNode.Error(kind = a.ctx.kind.key, cls = "ExportAssignment", msg = a.exp)
        }
        case a => throw new IllegalStateException(s"Unhandled ambient delaration [$a]")
      }
    }
    ret -> members.collect { case x: TypeScriptNode.VariableDecl if x.typ.t.isInstanceOf[FieldType.ObjectType] => x }
  }

  def print(ctx: ParseContext, config: ExportConfiguration, tsn: TypeScriptNode, file: ScalaFile, last: Boolean = false): Unit = {
    val h = MemberHelper(ctx, config, file)
    OutputHelper.printContext(file, tsn.ctx)
    tsn match {
      case node: TypeScriptNode.Constructor => if (node.ctx.isPublic) { file.add(s"def this(${node.params.map(h.forObj).mkString(", ")}) = this()") }
      case node: TypeScriptNode.VariableDecl => h.forDecl(node.name, node.typ, node.ctx)
      case node: TypeScriptNode.PropertyDecl => h.forDecl(node.name, node.typ, node.ctx)
      case node: TypeScriptNode.PropertySig => h.forDecl(node.name, node.typ, node.ctx)
      case node: TypeScriptNode.MethodDecl => h.forMethod(name = node.name, tParams = node.tParams, params = node.params, ret = node.ret, ctx = node.ctx)
      case node: TypeScriptNode.MethodSig => h.forMethod(name = node.name, tParams = node.tParams, params = node.params, ret = node.ret, ctx = node.ctx)
      case node: TypeScriptNode.TypeAliasDecl => file.add(s"type ${node.name} = ${FieldTypeAsScala.asScala(config = config, t = node.typ, isJs = true)}")
      case node: TypeScriptNode.IndexSig =>
        h.addImport(node.typ)
        val paramsString = node.params.map(h.forObj).mkString(", ")
        file.add("@js.annotation.JSBracketAccess")
        file.add(s"def apply($paramsString): ${FieldTypeAsScala.asScala(config = config, t = node.typ, isJs = true)} = js.native")
        if (!node.ctx.isConst) {
          file.add()
          file.add("@js.annotation.JSBracketAccess")
          file.add(s"def update($paramsString, v: ${FieldTypeAsScala.asScala(config = config, t = node.typ, isJs = true)}): Unit = js.native")
        }
      case node =>
        file.add(s"// ${node.getClass.getSimpleName}:${NodeHelper.asString(node)}")
    }
    // if (!last) { file.add() }
  }
}
