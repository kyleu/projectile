package com.kyleu.projectile.models.feature.service

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.{FeatureLogic, ModelFeature}
import com.kyleu.projectile.models.output.inject.{CommentProvider, TextSectionHelper}
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}

object InjectStartup extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "ProjectileModule.scala") {
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "models" :+ "module"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: Seq[String]) = {
    val filtered = config.models.filter(_.features(ModelFeature.Controller)).filter(_.inputType.isDatabase)
    val packages = filtered.flatMap(_.pkg.headOption).distinct

    def registerFor(pkg: String) = {
      s"""PermissionService.registerPackage("$pkg", "${ExportHelper.toClassName(pkg)}", Icons.pkg_$pkg)"""
    }

    val newLines = packages.sorted.map(registerFor)

    val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "injected startup code")
    TextSectionHelper.replaceBetween(filename = filename, original = original, p = params, newLines = newLines, project = config.project.key)
  }
}
