package com.projectile.models.feature.graphql

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.{EnumFeature, FeatureLogic, ModelFeature, ServiceFeature}
import com.projectile.models.output.{ExportHelper, OutputPath}

object InjectSchema extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "Schema.scala") {
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "graphql"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: String) = {
    val models = config.models.filter(_.features(ModelFeature.GraphQL))
    val services = config.services.filter(_.features(ServiceFeature.GraphQL))

    val (sStart, sEnd) = "    // Start service methods" -> "    // End service methods"
    def serviceFieldsFor(s: String) = {
      val newContent = services.map { s =>
        s"    ${(s.pkg :+ "services" :+ s"${s.className}Schema").mkString(".")}.serviceFields"
      }.sorted.mkString(" ++\n  ")
      ExportHelper.replaceBetween(filename = filename, original = s, start = sStart, end = sEnd, newContent = newContent)
    }

    val (fStart, fEnd) = "    // Start model fetchers" -> "    // End model fetchers"
    def fetcherFieldsFor(s: String) = if (markers.getOrElse("fetcher", Nil).isEmpty) {
      s
    } else {
      val newContent = "    Seq(\n" + markers.getOrElse("fetcher", Nil).sorted.map("      " + _.trim()).mkString(",\n") + "\n    )"
      ExportHelper.replaceBetween(filename = filename, original = s, start = fStart, end = fEnd, newContent = newContent)
    }

    val (eStart, eEnd) = "    // Start enum query fields" -> "    // End enum query fields"
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

    val (qStart, qEnd) = "    // Start model query fields" -> "    // End model query fields"
    def modelQueryFieldsFor(s: String) = {
      val newContent = models.map { m =>
        s"    ${(config.applicationPackage ++ m.modelPackage :+ m.className).mkString(".")}Schema.queryFields"
      }.sorted.mkString(" ++\n  ")
      ExportHelper.replaceBetween(filename = filename, original = s, start = qStart, end = qEnd, newContent = newContent)
    }

    val (mStart, mEnd) = "    // Start model mutation fields" -> "    // End model mutation fields"
    def mutationFieldsFor(s: String) = {
      val newContent = models.filter(_.pkFields.nonEmpty).map { m =>
        s"    ${(config.applicationPackage ++ m.modelPackage :+ m.className).mkString(".")}Schema.mutationFields"
      }.sorted.mkString(" ++\n  ")
      ExportHelper.replaceBetween(filename = filename, original = s, start = mStart, end = mEnd, newContent = newContent)
    }

    serviceFieldsFor(fetcherFieldsFor(enumQueryFieldsFor(modelQueryFieldsFor(mutationFieldsFor(original)))))
  }
}
