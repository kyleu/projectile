package com.kyleu.projectile.models.typescript.input

import better.files.File
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.input.{Input, InputSummary, InputTemplate}
import com.kyleu.projectile.models.output.file.{PlainTextFile, ScalaFile}
import com.kyleu.projectile.models.output.{ExportHelper, OutputPackage, OutputPath}
import com.kyleu.projectile.models.project.{Project, ProjectSummary, ProjectTemplate}
import com.kyleu.projectile.models.typescript.node.{NodeHelper, TypeScriptNode}
import com.kyleu.projectile.models.typescript.output.TypeScriptOutput
import com.kyleu.projectile.models.typescript.output.parse.{ParseContext, SourceFileParser}

object TypeScriptInput {
  def fromSummary(is: InputSummary, files: Seq[String]) = TypeScriptInput(key = is.key, description = is.description, files = files)
  def stripName(n: String) = n.stripSuffix(".ts").stripSuffix(".d")
}

final case class TypeScriptInput(
    override val key: String = "new",
    override val description: String = "...",
    files: Seq[String] = Nil,
    nodes: Seq[TypeScriptNode] = Nil,
    logs: Seq[String] = Nil
) extends Input {
  def fakeSummary() = ProjectSummary(key = TypeScriptInput.stripName(key), template = ProjectTemplate.ScalaLibrary, input = key)

  override val template = InputTemplate.TypeScript

  lazy val srcFiles = nodes.flatMap(NodeHelper.getSourceFileNodes)
  lazy val moduleReferences = nodes.flatMap(NodeHelper.getModuleReferenceNodes)

  lazy val output: ExportConfiguration = {
    val k = TypeScriptInput.stripName(key)
    val ctx = ParseContext(key = k, pkg = Nil, root = File("."))
    val p = Project(template = ProjectTemplate.Custom, k, k).copy(packages = Map(
      OutputPackage.Application -> Seq("com", "definitelyscala", ExportHelper.escapeKeyword(k))
    ))
    val indexFile = SourceFileParser.forSourceFiles(ctx, srcFiles, moduleReferences)
    val importFile = importScalaFile(p)
    val ec = ExportConfiguration(project = p).withAdditional(indexFile, importFile)
    TypeScriptOutput.forNodes(nodes = nodes, ctx = ctx, out = ec)
  }

  override lazy val enums = output.enums
  override lazy val models = output.models
  override lazy val unions = output.unions
  override lazy val services = output.services

  override lazy val additional = {
    val sources = srcFiles.map { f =>
      val path = f.path.split('/').map(_.trim).filter(_.nonEmpty)
      val dir = path.init.dropWhile(_ != key).toList match {
        case h :: t if h == key => t
        case x => x
      }
      val ret = PlainTextFile(path = OutputPath.TypeScriptOutput, dir = dir, key = path.last)
      f.ctx.src.foreach(ret.add(_))
      ret
    }

    output.additional ++ sources
  }

  private[this] def importScalaFile(p: Project) = {
    val file = ScalaFile(path = OutputPath.ServerSource, dir = p.packages(OutputPackage.Application), key = "Require")
    file.addImport(Seq("scala", "scalajs"), "js")
    file.add("@js.native")
    file.add(s"""@js.annotation.JSImport("${p.key}", js.annotation.JSImport.Namespace)""")
    file.add("object Require extends js.Object")
    file
  }
}
