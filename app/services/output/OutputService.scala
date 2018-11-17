package services.output

import better.files.File
import models.output.file.OutputFile
import models.project.ProjectOutput
import util.JsonSerializers._

object OutputService {
  object WriteResult {
    implicit val jsonEncoder: Encoder[WriteResult] = deriveEncoder
    implicit val jsonDecoder: Decoder[WriteResult] = deriveDecoder
  }

  case class WriteResult(file: String, path: String, logs: Seq[String])
}

class OutputService(projectRoot: File) {
  def persist(o: ProjectOutput, verbose: Boolean) = {
    o.featureOutput.flatMap { fo =>
      fo.files.map { f =>
        val result = write(f, o.getDirectory(projectRoot, f.path), verbose)
        OutputService.WriteResult(f.toString, result._1, result._2)
      } ++ fo.injections.map { i =>
        val f = o.getDirectory(projectRoot, i.path) / i.dir.mkString("/") / i.filename
        if (i.status == "OK") {
          if (f.exists && f.isReadable) {
            if (f.contentAsString != i.content) {
              f.overwrite(i.content)
              OutputService.WriteResult(f.toString, i.dir.mkString("/"), Seq(s"Overwrote [${util.NumberUtils.withCommas(i.content.length)}] bytes"))
            } else {
              OutputService.WriteResult(f.toString, i.dir.mkString("/"), Nil)
            }
          } else {
            throw new IllegalStateException(s"Cannot read file [${f.pathAsString}]")
          }
        } else {
          throw new IllegalStateException(s"Error encountered processing [${f.pathAsString}]: " + i.content)
        }
      }
    }
  }

  private[this] def write(file: OutputFile.Rendered, dir: File, verbose: Boolean) = {
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
