package services.output

import better.files.File
import models.output.file.OutputFile
import models.project.ProjectOutput

class OutputService(projectRoot: File) {
  def persist(o: ProjectOutput, verbose: Boolean) = {
    o.featureOutput.flatMap { fo =>
      fo.files.map { f =>
        val d = o.getDirectory(projectRoot, f.path)
        write(o.project.key, f, d, verbose)
      }
    }
  }

  private[this] def write(key: String, file: OutputFile.Rendered, dir: File, verbose: Boolean) = {
    val f = dir / (file.dir :+ file.filename).mkString("/")
    val existed = f.exists
    f.createFileIfNotExists(createParents = true)
    val path = projectRoot.relativize(f).toString
    val original = f.contentAsString
    if (original == file.content) {
      if (verbose) {
        path -> Seq(s"Ignoring unchanged file")
      } else {
        path -> Nil
      }
    } else if (original.isEmpty || original.contains("Generated File")) {
      f.overwrite(file.content)
      if (existed) {
        path -> Seq(s"Overwrote [${util.NumberUtils.withCommas(file.content.length)}] bytes")
      } else {
        path -> Seq(s"Created new [${util.NumberUtils.withCommas(file.content.length)}] byte file")
      }
    } else {
      if (verbose) {
        path -> Seq(s"Ignoring file as it has been modified, and is no longer generated")
      } else {
        path -> Nil
      }
    }
  }
}
