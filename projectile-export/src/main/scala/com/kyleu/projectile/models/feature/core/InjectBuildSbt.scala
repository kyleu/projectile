package com.kyleu.projectile.models.feature.core

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.FeatureLogic
import com.kyleu.projectile.models.input.InputType
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.inject.{CommentProvider, TextSectionHelper}

object InjectBuildSbt extends FeatureLogic.Inject(path = OutputPath.Root, filename = "build.sbt") {
  override def applies(config: ExportConfiguration) = config.models.exists(_.inputType == InputType.Model.TypeScriptModel)

  override def dir(config: ExportConfiguration) = Nil

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: Seq[String]) = {
    val r = markers.getOrElse("dependencies", Nil)
    val newLines = if (r.isEmpty) {
      Nil
    } else {
      "libraryDependencies ++= Seq(" +: r.flatMap { dep =>
        val sb = collection.mutable.ArrayBuffer.empty[String]
        val comma = if (r.lastOption.contains(dep)) { "" } else { "," }
        sb.append(s"""  "com.definitelyscala" %% "$dep" % "0.0.1"$comma""")
        sb
      } :+ ")"
    }
    val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "managed dependencies")
    if (original.exists(_.contains(params.start))) {
      TextSectionHelper.replaceBetween(filename = filename, original = original, p = params, newLines = newLines, project = config.project.key)
    } else {
      original
    }
  }
}
