package com.kyleu.projectile.models.typescript.output

import com.kyleu.projectile.models.export.{ExportEnum, ExportModel, ExportService, ExportUnion}
import com.kyleu.projectile.models.output.file.OutputFile
import com.kyleu.projectile.models.typescript.node.TypeScriptNode
import com.kyleu.projectile.models.typescript.output.parse._

object TypeScriptOutput {
  def forNodes(nodes: Seq[TypeScriptNode], ctx: ParseContext, out: TypeScriptOutput = TypeScriptOutput()): TypeScriptOutput = {
    nodes.foldLeft(out)((o, node) => forNode(ctx, o, node))
  }

  private[this] def forNode(ctx: ParseContext, out: TypeScriptOutput, node: TypeScriptNode): TypeScriptOutput = {
    val beforeParserResult = node match {
      case node: TypeScriptNode.ModuleDecl => ctx.plusPackage(node.name) -> out
      case _ => ctx -> out
    }
    val (parseCtx, parseOut) = beforeParserResult
    val parserResult = node match {
      case node: TypeScriptNode.ClassDecl => ClassParser.parse(parseCtx, parseOut, node)
      case node: TypeScriptNode.EnumDecl => EnumParser.parse(parseCtx, parseOut, node)
      case node: TypeScriptNode.InterfaceDecl => InterfaceParser.parse(parseCtx, parseOut, node)
      case _ => beforeParserResult
    }
    val childrenResult = parserResult._1 -> forNodes(nodes = node.children, ctx = parserResult._1, out = parserResult._2)
    val afterChildrenResult = childrenResult
    val finalResult = afterChildrenResult

    finalResult._2
  }
}

case class TypeScriptOutput(
    enums: Seq[ExportEnum] = Nil,
    models: Seq[ExportModel] = Nil,
    unions: Seq[ExportUnion] = Nil,
    services: Seq[ExportService] = Nil,
    additional: Seq[OutputFile] = Nil
) {
  def withEnums(e: ExportEnum*) = this.copy(enums = enums ++ e)
  def withModels(m: ExportModel*) = this.copy(models = models ++ m)
  def withUnions(u: ExportUnion*) = this.copy(unions = unions ++ u)
  def withServices(s: ExportService*) = this.copy(services = services ++ s)
  def withAdditional(a: OutputFile*) = this.copy(additional = additional ++ a)

  def merge(o: TypeScriptOutput) = {
    TypeScriptOutput(enums ++ o.enums, models ++ o.models, unions ++ o.unions, services ++ o.services, additional ++ o.additional)
  }
}
