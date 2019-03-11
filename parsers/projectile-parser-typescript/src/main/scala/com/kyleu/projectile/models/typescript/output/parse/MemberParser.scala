package com.kyleu.projectile.models.typescript.output.parse

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ._
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.typescript.node.{ModifierFlag, NodeHelper, TypeScriptNode}
import com.kyleu.projectile.models.typescript.output.OutputHelper

object MemberParser {
  def print(ctx: ParseContext, config: ExportConfiguration, tsn: TypeScriptNode, file: ScalaFile, last: Boolean = false): Unit = {
    val h = MemberHelper(ctx, config, file)
    OutputHelper.printContext(file, tsn.ctx)
    tsn match {
      case node: TypeScriptNode.Constructor => if (node.ctx.isPublic) { file.add(s"def this(${node.params.map(h.forObj).mkString(", ")}) = this()") }
      case node: TypeScriptNode.VariableDecl => h.forDecl(node.name, node.typ, node.ctx)
      case node: TypeScriptNode.PropertyDecl => h.forDecl(node.name, node.typ, node.ctx)
      case node: TypeScriptNode.MethodDecl => h.forMethod(name = node.name, tParams = node.tParams, params = node.params, ret = node.ret, ctx = node.ctx)
      case node: TypeScriptNode.MethodSig => h.forMethod(name = node.name, tParams = node.tParams, params = node.params, ret = node.ret, ctx = node.ctx)
      case node: TypeScriptNode.IndexSig =>
        h.addImport(node.typ)
        val paramsString = node.params.map(h.forObj).mkString(", ")
        file.add("@js.annotation.JSBracketAccess")
        file.add(s"def apply($paramsString): ${FieldTypeAsScala.asScala(config, node.typ)} = js.native")
        if (!node.ctx.isConst) {
          file.add()
          file.add("@js.annotation.JSBracketAccess")
          file.add(s"def update($paramsString, v: ${FieldTypeAsScala.asScala(config, node.typ)}): Unit = js.native")
        }
      case node =>
        file.add(s"// ${node.getClass.getSimpleName}:${NodeHelper.asString(node)}")
    }
    // if (!last) { file.add() }
  }
}
