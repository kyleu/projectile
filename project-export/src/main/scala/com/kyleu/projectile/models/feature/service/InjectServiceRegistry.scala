package com.kyleu.projectile.models.feature.service

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.{FeatureLogic, ModelFeature}
import com.kyleu.projectile.models.output.inject.{CommentProvider, TextSectionHelper}
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}

object InjectServiceRegistry extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "ServiceRegistry.scala") {
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "services"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: Seq[String]) = {
    val dbModels = config.models.filter(_.features(ModelFeature.Service)).filter(_.inputType.isDatabase)
    dbLogic(config.applicationPackage, dbModels, original, config.project.key)
  }

  private[this] def dbLogic(applicationPackage: Seq[String], models: Seq[ExportModel], original: Seq[String], projectKey: String) = {
    val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "model service files")
    val startIndex = original.indexOf(params.start)
    val endIndex = original.indexOf(params.end)

    val prefix = applicationPackage.map(_ + ".").mkString

    val newLines = {
      val withP = models.filter(_.pkg.nonEmpty).map(_.pkg.head).distinct.sorted
      val withoutP = models.filter(_.pkg.isEmpty).sortBy(_.key)

      val withPackages = withP.flatMap { p =>
        original.indexOf(s"val ${p}Services") match {
          case x if x > endIndex => None
          case x if x > -1 && x < startIndex => None
          case _ =>
            val comma = if (withoutP.isEmpty && withP.lastOption.contains(p)) { "" } else { "," }
            Some(s"""val ${p}Services: ${prefix}services.$p.${ExportHelper.toClassName(p)}ServiceRegistry$comma""")
        }
      }.sorted

      val withoutPackages = withoutP.flatMap { m =>
        original.indexOf(s"val ${m.propertyName}Service") match {
          case x if x > -1 && x < startIndex => None
          case x if x > endIndex => None
          case _ =>
            val comma = if (withoutP.lastOption.contains(m)) { "" } else { "," }
            Some(s"""val ${m.propertyName}Service: ${prefix}services.${m.className}Service$comma""")
        }
      }.sorted

      val separator = if (withPackages.nonEmpty && withoutPackages.nonEmpty) { Seq("") } else { Nil }
      withPackages ++ separator ++ withoutPackages
    }

    TextSectionHelper.replaceBetween(filename = filename, original = original, p = params, newLines = newLines, project = projectKey)
  }
}
