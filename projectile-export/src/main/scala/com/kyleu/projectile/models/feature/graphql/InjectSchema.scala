package com.kyleu.projectile.models.feature.graphql

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature, ServiceFeature}
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.inject.{CommentProvider, TextSectionHelper}

object InjectSchema extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "Schema.scala") {
  override def applies(config: ExportConfiguration) = config.models.exists(_.features(ModelFeature.GraphQL))
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "models" :+ "graphql"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: Seq[String]) = {
    val enums = config.enums.filter(e => e.features(EnumFeature.GraphQL) && e.inputType.isDatabase).sortBy(e => e.modelPackage(config).mkString + e.className)
    val models = config.models.filter { m =>
      m.features(ModelFeature.GraphQL) && m.inputType.isDatabase
    }.sortBy(m => m.modelPackage(config).mkString + m.className)
    val services = config.services.filter(s => s.features(ServiceFeature.GraphQL) && s.inputType.isDatabase).sortBy(s => s.pkg.mkString + s.className)

    def fetcherFieldsFor(s: Seq[String]) = {
      val fetchers = markers.getOrElse("fetcher", Nil).sorted
      if (fetchers.isEmpty) {
        s
      } else {
        val newLines = "Seq(" +: fetchers.map { f =>
          val concat = if (fetchers.lastOption.contains(f)) { "" } else ","
          "  " + f.trim() + concat
        } :+ ") ++"

        val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "model fetchers")
        TextSectionHelper.replaceBetween(filename = filename, original = s, p = params, newLines = newLines, project = config.project.key)
      }
    }

    def queryFieldsFor(s: Seq[String]) = {
      val newLines = Seq(
        enums.map(e => s"${e.graphqlPackage(config).map(_ + ".").mkString}${e.className}Schema.queryFields ++").sorted,
        models.map(m => s"${(m.graphqlPackage(config) :+ m.className).mkString(".")}Schema.queryFields ++").sorted,
        services.map(s => s"${(s.pkg :+ s"${s.className}Schema").mkString(".")}.serviceFields ++").sorted
      ).flatten
      val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "query fields")
      TextSectionHelper.replaceBetween(filename = filename, original = s, p = params, newLines = newLines, project = config.project.key)
    }

    def mutationFieldsFor(s: Seq[String]) = {
      val newLines = Seq(
        models.filter(_.pkFields.nonEmpty).map(m => s"${(m.graphqlPackage(config) :+ m.className).mkString(".")}Schema.mutationFields ++")
      ).flatten
      val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "mutation fields")
      TextSectionHelper.replaceBetween(filename = filename, original = s, p = params, newLines = newLines, project = config.project.key)
    }

    val postFetcher = fetcherFieldsFor(original)
    val postQuery = queryFieldsFor(postFetcher)
    val postMutation = mutationFieldsFor(postQuery)
    postMutation
  }
}
