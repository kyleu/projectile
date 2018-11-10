package services.project.audit

import better.files._
import models.export.config.ExportConfiguration
import models.project.ProjectOutput

object ExportValidation {
  def validate(projectRoot: File, config: ExportConfiguration, result: ProjectOutput) = {
    val out = result.files.map(f => result.getDirectory(projectRoot, f.path))
    val files = getGeneratedFiles(projectRoot).distinct.map { f =>
      val s = projectRoot.relativize(f.path).toString.stripPrefix("/")
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

  private[this] val badBoys = Set("target", "public", ".idea", ".git", "project")

  private[this] def getGeneratedFiles(f: File): Seq[File] = {
    if (!f.isDirectory) { throw new IllegalStateException(s"[$f] is not a directory.") }
    f.children.toSeq.flatMap {
      case child if badBoys(child.name) => Nil
      case child if child.isDirectory => getGeneratedFiles(child)
      case child if child.contentAsString.contains("Generated File") => Seq(child)
      case _ => Nil
    }
  }
}
