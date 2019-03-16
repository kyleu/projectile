package com.kyleu.projectile.services.project

import better.files.File
import com.kyleu.projectile.models.output.OutputWriteResult
import com.kyleu.projectile.models.output.file.OutputFile
import com.kyleu.projectile.models.project.ProjectOutput
import com.kyleu.projectile.services.ProjectileService
import com.kyleu.projectile.services.config.ConfigService
import com.kyleu.projectile.util.NumberUtils

class OutputService(svc: ProjectileService) {
  def persist(o: ProjectOutput, verbose: Boolean, cfg: ConfigService) = {
    o.featureOutput.flatMap { fo =>
      fo.files.map { f =>
        val result = write(cfg, f, o.getDirectory(cfg.workingDirectory, f.path), verbose)
        OutputWriteResult(f.toString, result._1, result._2)
      } ++ fo.injections.map { i =>
        val f = o.getDirectory(cfg.workingDirectory, i.path) / i.dir.mkString("/") / i.filename
        if (i.status == "OK") {
          if (f.exists && f.isReadable) {
            val p = i.toString
            if (f.contentAsString != i.content) {
              f.overwrite(i.content)
              OutputWriteResult(p, i.dir.mkString("/"), Seq(s"Overwrote [${NumberUtils.withCommas(i.content.length)}] bytes"))
            } else if (verbose) {
              OutputWriteResult(p, i.dir.mkString("/"), Seq("Ignoring unchanged file"))
            } else {
              OutputWriteResult(p, i.dir.mkString("/"), Nil)
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
        path -> Seq("Ignoring unchanged file")
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
        path -> Seq("Ignoring file as it has been modified, and is no longer generated")
      } else {
        path -> Nil
      }
    }
  }
}
