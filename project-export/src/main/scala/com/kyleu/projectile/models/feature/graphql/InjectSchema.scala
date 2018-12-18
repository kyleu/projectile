package com.kyleu.projectile.models.feature.graphql

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature, ServiceFeature}
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.inject.{CommentProvider, TextSectionHelper}

object InjectSchema extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "Schema.scala") {
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "graphql"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: Seq[String]) = {
    val enums = config.enums.filter(_.features(EnumFeature.GraphQL)).sortBy(e => e.modelPackage.mkString + e.className)
    val models = config.models.filter(_.features(ModelFeature.GraphQL)).sortBy(m => m.modelPackage.mkString + m.className)
    val services = config.services.filter(_.features(ServiceFeature.GraphQL)).sortBy(s => s.pkg.mkString + s.className)

    def serviceFieldsFor(s: Seq[String]) = {
      val newLines = if (services.isEmpty) { Seq("Nil") } else {
        services.map { s =>
          val prelude = if (services.headOption.contains(s)) { "" } else "  "
          val concat = if (services.lastOption.contains(s)) { "" } else " ++"
          s"$prelude${(s.pkg :+ "services" :+ s"${s.className}Schema").mkString(".")}.serviceFields$concat"
        }.sorted
      }

      val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "service methods")
      TextSectionHelper.replaceBetween(filename = filename, original = s, p = params, newLines = newLines, project = config.project.key)
    }

    def fetcherFieldsFor(s: Seq[String]) = {
      val fetchers = markers.getOrElse("fetcher", Nil).sorted
      val newLines = "Seq(" +: fetchers.map { f =>
        val concat = if (fetchers.lastOption.contains(f)) { "" } else ","
        "  " + f.trim() + concat
      } :+ ")"

      val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "model fetchers")
      TextSectionHelper.replaceBetween(filename = filename, original = s, p = params, newLines = newLines, project = config.project.key)
    }

    def enumQueryFieldsFor(s: Seq[String]) = {
      val newLines = enums.map { e =>
        val prelude = if (enums.headOption.contains(e)) { "" } else "  "
        val concat = if (enums.lastOption.contains(e)) { "" } else " ++"
        s"$prelude${(config.applicationPackage ++ e.modelPackage).map(_ + ".").mkString}${e.className}Schema.queryFields$concat"
      }

      val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "enum query fields")
      TextSectionHelper.replaceBetween(filename = filename, original = s, p = params, newLines = newLines, project = config.project.key)
    }

    def modelQueryFieldsFor(s: Seq[String]) = {
      val newLines = models.map { m =>
        val prelude = if (models.headOption.contains(m)) { "" } else "  "
        val concat = if (models.lastOption.contains(m)) { "" } else " ++"
        s"$prelude${(config.applicationPackage ++ m.modelPackage :+ m.className).mkString(".")}Schema.queryFields$concat"
      }

      val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "model query fields")
      TextSectionHelper.replaceBetween(filename = filename, original = s, p = params, newLines = newLines, project = config.project.key)
    }

    def mutationFieldsFor(s: Seq[String]) = {
      val newLines = models.filter(_.pkFields.nonEmpty).map { m =>
        val prelude = if (models.headOption.contains(m)) { "" } else "  "
        val concat = if (models.lastOption.contains(m)) { "" } else " ++"
        s"$prelude${(config.applicationPackage ++ m.modelPackage :+ m.className).mkString(".")}Schema.mutationFields$concat"
      }

      val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "model mutation fields")
      TextSectionHelper.replaceBetween(filename = filename, original = s, p = params, newLines = newLines, project = config.project.key)
    }

    val postMutation = mutationFieldsFor(original)
    val postModel = modelQueryFieldsFor(postMutation)
    val postEnum = enumQueryFieldsFor(postModel)
    val postFetcher = fetcherFieldsFor(postEnum)
    serviceFieldsFor(postFetcher)
  }
}
