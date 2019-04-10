package com.kyleu.projectile.models.typescript.output.parse

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.{FieldType, FieldTypeRequired, ObjectField, TypeParam}
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}
import com.kyleu.projectile.models.typescript.node.{NodeContext, TypeScriptNode}
import com.kyleu.projectile.models.typescript.output.OutputHelper

object ObjectTypeParser {
  def parseLiteral(ctx: ParseContext, config: ExportConfiguration, name: String, o: FieldType.ObjectType, req: Boolean, nodeCtx: NodeContext) = {
    parse(ctx = ctx, config = config, name = name, tParams = o.tParams, nodeCtx = nodeCtx, fields = o.fields)
  }

  def parse(ctx: ParseContext, config: ExportConfiguration, name: String, tParams: Seq[TypeParam], nodeCtx: NodeContext, fields: Seq[ObjectField]) = {
    val cn = ExportHelper.toClassName(name)

    val file = ScalaFile(path = OutputPath.SharedSource, dir = config.mergedApplicationPackage(ctx.pkg), key = cn)
    OutputHelper.printContext(file, nodeCtx)
    MemberHelper.addGlobal(file, config, ctx, Some(name))
    file.add(s"object $cn extends js.Object {", 1)
    fields.foreach { f =>
      val node = TypeScriptNode.VariableDecl(name = f.k, typ = FieldTypeRequired(f.t, f.req), ctx = NodeContext.empty)
      MemberParser.print(ctx = ctx, config = config, tsn = node, file = file, last = fields.lastOption.contains(f))
    }
    file.add("}", -1)
    file.add()

    ctx -> config.withAdditional(file)
  }
}
