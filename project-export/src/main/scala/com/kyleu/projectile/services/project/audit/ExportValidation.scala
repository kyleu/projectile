package com.kyleu.projectile.services.project.audit

import better.files._
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.project.ProjectOutput

object ExportValidation {
  def validate(projectRoot: File, config: ExportConfiguration, result: ProjectOutput) = {
    val root = result.getDirectory(projectRoot, OutputPath.Root)
    val out = result.files.map { f =>
      result.getDirectory(projectRoot, f.path) / f.filePath
    }
    val files = getGeneratedFiles(root).distinct.map { f =>
      val s = root.relativize(f.path).toString.stripPrefix("/")
      f -> s
    }
    files.flatMap { f =>
      if (out.contains(f._1)) {
        None
      } else {
        // f._1.delete()
        Some(f._2 -> "Untracked")
      }
    }
  }
  private[this] val magicWord = "Generated File"
  private[this] val badBoys = Set("target", "public", ".idea", ".git", "project", "charts")
  private[this] val extensions = Set("scala", "md", "graphql", "json", "routes", "html", "thrift").map("." + _)

  private[this] def getGeneratedFiles(f: File): Seq[File] = {
    if (!f.isDirectory) { throw new IllegalStateException(s"[$f] is not a directory.") }
    f.children.toSeq.flatMap {
      case child if badBoys(child.name) => Nil
      case child if child.isDirectory => getGeneratedFiles(child)
      case child if extensions.exists(child.name.endsWith) => if (child.contentAsString.contains(magicWord)) {
        Seq(child)
      } else {
        Nil
      }
      case _ => Nil
    }
  }
}
