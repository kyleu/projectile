package com.kyleu.projectile.models.typescript.output

import com.kyleu.projectile.models.export.{ExportEnum, ExportModel, ExportService, ExportUnion}
import com.kyleu.projectile.models.output.file.OutputFile
import com.kyleu.projectile.models.typescript.node.TypeScriptNode
import com.kyleu.projectile.models.typescript.output.parse._

object TypeScriptOutput {
  def forNodes(nodes: Seq[TypeScriptNode], ctx: ParseContext, out: TypeScriptOutput = TypeScriptOutput()): TypeScriptOutput = {
    val loaded = nodes.foldLeft(out)((o, node) => loadNode(ctx, o, node)._2)
    nodes.foldLeft(loaded)((o, node) => parseNode(ctx, o, node)._2)
  }

  private[this] def newCtx(ctx: ParseContext, node: TypeScriptNode): ParseContext = node match {
    case node: TypeScriptNode.ModuleDecl => ctx.plusPackage(node.name)
    case _ => ctx
  }

  private[this] def loadNode(ctx: ParseContext, out: TypeScriptOutput, node: TypeScriptNode): (ParseContext, TypeScriptOutput) = {
    val c = newCtx(ctx, node)
    val loadResult = node match {
      case node: TypeScriptNode.EnumDecl => EnumParser.load(c, out, node)
      case _ => c -> out
    }
    node.children.foldLeft(loadResult)((o, node) => loadNode(ctx, o._2, node))
  }

  private[this] def parseNode(ctx: ParseContext, out: TypeScriptOutput, node: TypeScriptNode): (ParseContext, TypeScriptOutput) = {
    val c = newCtx(ctx, node)
    val parserResult = node match {
      case node: TypeScriptNode.ClassDecl => ClassParser.parse(c, out, node)
      case node: TypeScriptNode.EnumDecl => EnumParser.parse(c, out, node)
      case node: TypeScriptNode.InterfaceDecl => InterfaceParser.parse(c, out, node)
      case _ => c -> out
    }
    node.children.foldLeft(parserResult)((o, node) => parseNode(ctx, o._2, node))
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
