package com.kyleu.projectile.models.typescript.output.parse

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ._
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.typescript.node.{ModifierFlag, NodeContext, NodeHelper, TypeScriptNode}
import com.kyleu.projectile.models.typescript.output.OutputHelper
import io.circe.Json

object MemberParser {
  def print(ctx: ParseContext, config: ExportConfiguration, tsn: TypeScriptNode, file: ScalaFile, last: Boolean = false) = {
    def addImport(t: FieldType) = FieldTypeImports.imports(config, t).foreach(pkg => file.addImport(pkg.init, pkg.last))
    def forType(typ: FieldTypeRequired) = {
      addImport(typ.t)
      val s = FieldTypeAsScala.asScala(config, typ.t)
      if (typ.r) { s } else { s"Option[$s}]" }
    }
    def forObj(obj: ObjectField) = {
      addImport(obj.t)
      val s = FieldTypeAsScala.asScala(config, obj.t)
      s"${obj.k}: ${if (obj.req) { s } else { s"Option[$s}]" }}"
    }
    def forMethod(name: String, tParams: Seq[Json], params: Seq[ObjectField], ret: FieldTypeRequired, ctx: NodeContext) = {
      val paramsString = params.map(forObj).mkString(", ")
      val tParamsString = if (tParams.isEmpty) { "" } else { "[" + tParams.map(_ => "T").mkString(", ") + "]" }
      val abst = ctx.modifiers(ModifierFlag.Abstract)
      val decl = if (abst) { "" } else { " = js.native" }
      file.add(s"def $name$tParamsString($paramsString): ${forType(ret)}$decl")
    }

    OutputHelper.printContext(file, tsn.ctx)
    tsn match {
      case node: TypeScriptNode.PropertyDecl =>
        val abst = node.ctx.modifiers(ModifierFlag.Abstract)
        val decl = if (abst) { "" } else { " = js.native" }
        val cnst = node.ctx.modifiers(ModifierFlag.Readonly)
        val keyword = if (cnst) { "val" } else { "var" }
        file.add(s"$keyword ${node.name}: ${forType(node.typ)}$decl")
      case node: TypeScriptNode.MethodDecl => forMethod(name = node.name, tParams = node.tParams, params = node.params, ret = node.ret, ctx = node.ctx)
      case node: TypeScriptNode.MethodSig => forMethod(name = node.name, tParams = node.tParams, params = node.params, ret = node.ret, ctx = node.ctx)
      case node =>
        file.add(s"// ${node.getClass.getSimpleName}:${NodeHelper.asString(node)}")
    }
    if (!last) { file.add() }
  }
}
