package com.kyleu.projectile.services.output

import better.files.File
import com.kyleu.projectile.util.NumberUtils
import com.kyleu.projectile.models.output.file.OutputFile
import com.kyleu.projectile.models.project.ProjectOutput
import com.kyleu.projectile.services.ProjectileService
import com.kyleu.projectile.services.config.ConfigService
import com.kyleu.projectile.util.JsonSerializers._

object OutputService {
  object WriteResult {
    implicit val jsonEncoder: Encoder[WriteResult] = deriveEncoder
    implicit val jsonDecoder: Decoder[WriteResult] = deriveDecoder
  }

  case class WriteResult(file: String, path: String, logs: Seq[String])
}

class OutputService(svc: ProjectileService) {
  def persist(o: ProjectOutput, verbose: Boolean) = {
    val cfg = svc.configForProject(o.project.key)
    o.featureOutput.flatMap { fo =>
      fo.files.map { f =>
        val result = write(cfg, f, o.getDirectory(cfg.workingDirectory, f.path), verbose)
        OutputService.WriteResult(f.toString, result._1, result._2)
      } ++ fo.injections.map { i =>
        val f = o.getDirectory(cfg.workingDirectory, i.path) / i.dir.mkString("/") / i.filename
        if (i.status == "OK") {
          if (f.exists && f.isReadable) {
            val p = i.toString
            if (f.contentAsString != i.content) {
              f.overwrite(i.content)
              OutputService.WriteResult(p, i.dir.mkString("/"), Seq(s"Overwrote [${NumberUtils.withCommas(i.content.length)}] bytes"))
            } else if (verbose) {
              OutputService.WriteResult(p, i.dir.mkString("/"), Seq(s"Ignoring unchanged file"))
            } else {
              OutputService.WriteResult(p, i.dir.mkString("/"), Nil)
            }
          } else {
            throw new IllegalStateException(s"Cannot read file [${f.pathAsString}]")
          }
        } else {
          throw new IllegalStateException(s"Error [${i.status}] encountered processing [${f.pathAsString}]: " + i.content)
        }
      }
    }
  }

  private[this] def write(cfg: ConfigService, file: OutputFile.Rendered, dir: File, verbose: Boolean) = {
    val f = dir / (file.dir :+ file.filename).mkString("/")
    val existed = f.exists
    f.createFileIfNotExists(createParents = true)
    val path = cfg.workingDirectory.relativize(f).toString
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
        path -> Seq(s"Overwrote [${NumberUtils.withCommas(file.content.length)}] bytes")
      } else {
        path -> Seq(s"Created new [${NumberUtils.withCommas(file.content.length)}] byte file")
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
