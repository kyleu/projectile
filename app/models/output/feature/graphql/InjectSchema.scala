package models.output.feature.graphql

import models.export.config.ExportConfiguration
import models.output.feature.{EnumFeature, FeatureLogic, ModelFeature}
import models.output.{ExportHelper, OutputPath}

object InjectSchema extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "Schema.scala") {
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "graphql"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: String) = {
    val models = config.models.filter(_.features(ModelFeature.GraphQL))

    def enumQueryFieldsFor(s: String) = {
      val newContent = if (!config.enums.exists(_.features(EnumFeature.GraphQL))) {
        "    Seq.empty[Field[GraphQLContext, Unit]]"
      } else {
        config.enums.filter(_.features(EnumFeature.GraphQL)).map { e =>
          s"    ${(config.applicationPackage ++ e.modelPackage).map(_ + ".").mkString}${e.className}Schema.queryFields"
        }.sorted.mkString(" ++\n  ")
      }
      ExportHelper.replaceBetween(filename = "TODO", original = s, start = "    // Start enum query fields", end = s"    // End enum query fields", newContent = newContent)
    }

    def modelQueryFieldsFor(s: String) = {
      val newContent = models.map { m =>
        s"    ${(config.applicationPackage ++ m.modelPackage).map(_ + ".").mkString}${m.className}Schema.queryFields"
      }.sorted.mkString(" ++\n  ")
      ExportHelper.replaceBetween(filename = "TODO", original = s, start = "    // Start model query fields", end = s"    // End model query fields", newContent = newContent)
    }

    def mutationFieldsFor(s: String) = {
      val newContent = models.filter(_.pkFields.nonEmpty).map { m =>
        s"    ${(config.applicationPackage ++ m.modelPackage).map(_ + ".").mkString}${m.className}Schema.mutationFields"
      }.sorted.mkString(" ++\n  ")
      ExportHelper.replaceBetween(
        filename = "TODO",
        original = s,
        start = "    // Start model mutation fields",
        end = s"    // End model mutation fields",
        newContent = newContent
      )
    }

    def fetcherFieldsFor(s: String) = if (markers.getOrElse("fetcher", Nil).isEmpty) {
      s
    } else {
      val newContent = "    Seq(\n" + markers.getOrElse("fetcher", Nil).sorted.map("      " + _.trim()).mkString(",\n") + "\n    )"
      ExportHelper.replaceBetween(filename = "TODO", original = s, start = "    // Start model fetchers", end = s"    // End model fetchers", newContent = newContent)
    }

    fetcherFieldsFor(enumQueryFieldsFor(modelQueryFieldsFor(mutationFieldsFor(original))))
  }
}
