package com.kyleu.projectile.models.typescript.input

import better.files.File
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.input.{Input, InputSummary, InputTemplate}
import com.kyleu.projectile.models.project.{Project, ProjectSummary, ProjectTemplate}
import com.kyleu.projectile.models.typescript.node.{NodeHelper, TypeScriptNode}
import com.kyleu.projectile.models.typescript.output.TypeScriptOutput
import com.kyleu.projectile.models.typescript.output.parse.{ParseContext, SourceFileParser}

object TypeScriptInput {
  def fromSummary(is: InputSummary, files: Seq[String]) = TypeScriptInput(key = is.key, description = is.description, files = files)
  def stripName(n: String) = n.stripSuffix(".ts").stripSuffix(".d")
}

case class TypeScriptInput(
    override val key: String = "new",
    override val description: String = "...",
    files: Seq[String] = Nil,
    nodes: Seq[TypeScriptNode] = Nil,
    logs: Seq[String] = Nil
) extends Input {
  def fakeSummary() = ProjectSummary(key = key + "-generated", template = ProjectTemplate.ScalaLibrary, input = key)

  override val template = InputTemplate.TypeScript

  lazy val output = {
    val k = TypeScriptInput.stripName(key)
    val ctx = ParseContext(key = k, pkg = Nil, root = File("."))
    val indexFile = SourceFileParser.forSourceFiles(ctx, nodes.flatMap(NodeHelper.getSourceFileNodes))
    val p = Project(template = ProjectTemplate.Custom, key + "-generated", key)
    val ec = ExportConfiguration(project = p).withAdditional(indexFile)
    TypeScriptOutput.forNodes(nodes = nodes, ctx = ctx, out = ec)
  }

  override lazy val enums = output.enums
  override lazy val models = output.models
  override lazy val unions = output.unions
  override lazy val services = output.services
  override lazy val additional = output.additional
}
