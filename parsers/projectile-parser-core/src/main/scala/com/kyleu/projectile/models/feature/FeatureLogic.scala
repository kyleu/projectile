package com.kyleu.projectile.models.feature

import better.files.File
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.{InjectResult, OutputFile}
import com.kyleu.projectile.util.StringUtils

import scala.util.control.NonFatal

object FeatureLogic {
  abstract class Inject(val path: OutputPath, val filename: String) {
    def dir(config: ExportConfiguration): Seq[String]
    def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: Seq[String]): Seq[String]

    def inject(config: ExportConfiguration, markers: Map[String, Seq[String]], projectRoot: File, info: String => Unit, debug: String => Unit) = {
      val projectPath = projectRoot / config.project.getPath(path)
      val d = dir(config).mkString("/")
      val f = projectPath / d / filename

      if (f.isRegularFile && f.isReadable) {
        val (status, newContent) = try {
          "OK" -> logic(config, markers, StringUtils.lines(f.contentAsString))
        } catch {
          case NonFatal(x) => "Error" -> Seq(x.toString)
        }

        debug(s"Injected $filename")
        Seq(InjectResult(
          path = path,
          dir = dir(config),
          filename = filename,
          status = status,
          content = newContent.map(_ + "\n").mkString
        ))
      } else {
        info(s"Cannot load file [${f.pathAsString}] for injection")
        Nil
      }
    }
  }
}

trait FeatureLogic {
  def export(
    config: ExportConfiguration,
    info: String => Unit,
    debug: String => Unit
  ): Seq[OutputFile.Rendered] = Nil

  def injections: Seq[FeatureLogic.Inject] = Nil

  final def inject(
    config: ExportConfiguration,
    projectRoot: File,
    markers: Map[String, Seq[String]],
    info: String => Unit,
    debug: String => Unit
  ): Seq[InjectResult] = injections.flatMap(_.inject(config, markers, projectRoot, info, debug))
}
