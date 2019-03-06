package com.kyleu.projectile.models.typescript.output

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.typescript.node.TypeScriptNode
import com.kyleu.projectile.models.typescript.output.parse._

object TypeScriptOutput {
  def forNodes(nodes: Seq[TypeScriptNode], ctx: ParseContext, out: ExportConfiguration): ExportConfiguration = {
    val loaded = nodes.foldLeft(out)((o, node) => loadNode(ctx, o, node)._2)
    nodes.foldLeft(loaded)((o, node) => parseNode(ctx, o, node)._2)
  }

  private[this] def newCtx(ctx: ParseContext, node: TypeScriptNode): ParseContext = node match {
    case node: TypeScriptNode.ModuleDecl => ctx.plusPackage(node.name)
    case _ => ctx
  }

  private[this] def loadNode(ctx: ParseContext, out: ExportConfiguration, node: TypeScriptNode): (ParseContext, ExportConfiguration) = {
    val c = newCtx(ctx, node)
    val loadResult = node match {
      case node: TypeScriptNode.EnumDecl => EnumParser.load(c, out, node)
      case _ => c -> out
    }
    node.children.foldLeft(loadResult)((o, node) => loadNode(ctx, o._2, node))
  }

  private[this] def parseNode(ctx: ParseContext, out: ExportConfiguration, node: TypeScriptNode): (ParseContext, ExportConfiguration) = {
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
