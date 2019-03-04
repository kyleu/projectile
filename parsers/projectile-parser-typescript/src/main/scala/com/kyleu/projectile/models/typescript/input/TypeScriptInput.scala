package com.kyleu.projectile.models.typescript.input

import better.files.File
import com.kyleu.projectile.models.input.{Input, InputSummary, InputTemplate}
import com.kyleu.projectile.models.typescript.node.{NodeHelper, TypeScriptNode}
import com.kyleu.projectile.models.typescript.output.TypeScriptOutput
import com.kyleu.projectile.models.typescript.output.parse.{ParseContext, SourceFileParser}

object TypeScriptInput {
  def fromSummary(is: InputSummary, files: Seq[String]) = TypeScriptInput(key = is.key, description = is.description, files = files)
}

case class TypeScriptInput(
    override val key: String = "new",
    override val description: String = "...",
    files: Seq[String] = Nil,
    nodes: Seq[TypeScriptNode] = Nil,
    logs: Seq[String] = Nil
) extends Input {
  override val template = InputTemplate.TypeScript

  lazy val output = {
    val k = key.stripSuffix(".ts").stripSuffix(".d")
    val ctx = ParseContext(key = k, pkg = List(k), root = File("."))
    val indexFile = SourceFileParser.forSourceFiles(ctx, nodes.flatMap(NodeHelper.getSourceFileNodes))
    TypeScriptOutput.forNodes(nodes = nodes, ctx = ctx).withAdditional(indexFile)
  }

  override lazy val enums = output.enums
  override lazy val models = output.models
  override lazy val unions = output.unions
  override lazy val services = output.services
  override lazy val additional = output.additional
}
