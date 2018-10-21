package services.output

import better.files.File
import models.output.file.OutputFile
import models.project.ProjectOutput

class OutputService(projectRoot: File) {
  def persist(o: ProjectOutput, verbose: Boolean) = {
    o.featureOutput.flatMap { fo =>
      fo.files.map { f =>
        val d = o.getDirectory(projectRoot, f.path)
        val logs = Seq.empty[String]
        (f.filename, write(o.project.key, f, d, verbose))
      }
    }
  }

  private[this] def write(key: String, file: OutputFile.Rendered, dir: File, verbose: Boolean) = {
    val f = dir / (file.dir :+ file.filename).mkString("/")
    f.createFileIfNotExists(createParents = true)
    val original = f.contentAsString
    if (original == file.content) {
      if (verbose) {
        Seq(s"Ignoring unchanged file")
      } else {
        Nil
      }
    } else if (original.isEmpty || original.contains("Generated File")) {
      f.overwrite(file.content)
      Seq(s"Overwrote [${util.NumberUtils.withCommas(file.content.length)}] bytes")
    } else {
      if (verbose) {
        Seq(s"Ignoring file as it has been modified, and is no longer generated")
      } else {
        Nil
      }
    }
  }
}
