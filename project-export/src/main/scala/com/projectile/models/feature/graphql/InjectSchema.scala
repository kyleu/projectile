package com.projectile.models.feature.graphql

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature}
import com.projectile.models.output.{ExportHelper, OutputPath}

object InjectSchema extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "Schema.scala") {
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "graphql"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: String) = {
    val models = config.models.filter(_.features(ModelFeature.GraphQL))

    val (eStart, eEnd) = "    // Start enum query fields" -> "    // End enum query fields"
    val (qStart, qEnd) = "    // Start model query fields" -> "    // End model query fields"
    val (mStart, mEnd) = "    // Start model mutation fields" -> "    // End model mutation fields"
    val (fStart, fEnd) = "    // Start model fetchers" -> "    // End model fetchers"

    def enumQueryFieldsFor(s: String) = {
      val newContent = if (!config.enums.exists(_.features(EnumFeature.GraphQL))) {
        "    Seq.empty[Field[GraphQLContext, Unit]]"
      } else {
        config.enums.filter(_.features(EnumFeature.GraphQL)).map { e =>
          s"    ${(config.applicationPackage ++ e.modelPackage).map(_ + ".").mkString}${e.className}Schema.queryFields"
        }.sorted.mkString(" ++\n  ")
      }
      ExportHelper.replaceBetween(filename = filename, original = s, start = eStart, end = eEnd, newContent = newContent)
    }

    def modelQueryFieldsFor(s: String) = {
      val newContent = models.map { m =>
        s"    ${(config.applicationPackage ++ m.modelPackage).map(_ + ".").mkString}${m.className}Schema.queryFields"
      }.sorted.mkString(" ++\n  ")
      ExportHelper.replaceBetween(filename = filename, original = s, start = qStart, end = qEnd, newContent = newContent)
    }

    def mutationFieldsFor(s: String) = {
      val newContent = models.filter(_.pkFields.nonEmpty).map { m =>
        s"    ${(config.applicationPackage ++ m.modelPackage).map(_ + ".").mkString}${m.className}Schema.mutationFields"
      }.sorted.mkString(" ++\n  ")
      ExportHelper.replaceBetween(filename = filename, original = s, start = mStart, end = mEnd, newContent = newContent)
    }

    def fetcherFieldsFor(s: String) = if (markers.getOrElse("fetcher", Nil).isEmpty) {
      s
    } else {
      val newContent = "    Seq(\n" + markers.getOrElse("fetcher", Nil).sorted.map("      " + _.trim()).mkString(",\n") + "\n    )"
      ExportHelper.replaceBetween(filename = filename, original = s, start = fStart, end = fEnd, newContent = newContent)
    }

    fetcherFieldsFor(enumQueryFieldsFor(modelQueryFieldsFor(mutationFieldsFor(original))))
  }
}
