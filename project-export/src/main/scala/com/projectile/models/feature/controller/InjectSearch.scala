package com.projectile.models.feature.controller

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.{ExportHelper, OutputPath}
import com.projectile.models.feature.FeatureLogic
import com.projectile.models.feature.service.InjectSearchParams

object InjectSearch extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "SearchController.scala") {
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "controllers" :+ "admin" :+ "system"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: String) = {
    def searchStringFieldsFor(s: String) = {
      val stringModels = markers.getOrElse("string-search", Nil).map(s => InjectSearchParams(config.getModel(s)))

      if (stringModels.isEmpty) {
        s
      } else {
        val (sStart, sEnd) = "    // Start string searches" -> "    // End string searches"
        val stringFields = stringModels.map { m =>
          s"    val ${m.model.propertyName} = ${m.model.serviceReference}.searchExact(creds, q = q, limit = Some(5)).map(_.map { model =>\n" ++
            s"      ${m.viewClass}(model, ${m.message})\n" +
            "    })"
        }
        val stringFutures = stringModels.map(_.model.propertyName).mkString(", ")
        val newContent = stringFields.sorted.mkString("\n") + s"\n\n    val stringSearches = Seq[Future[Seq[Html]]]($stringFutures)"
        ExportHelper.replaceBetween(filename = filename, original = s, start = sStart, end = sEnd, newContent = newContent)
      }
    }

    def searchIntFieldsFor(s: String) = {
      val intModels = markers.getOrElse("int-search", Nil).map(s => InjectSearchParams(config.getModel(s)))

      if (intModels.isEmpty) {
        s
      } else {
        val (sStart, sEnd) = "    // Start int searches" -> "    // End int searches"
        val intFields = intModels.map { m =>
          s"    val ${m.model.propertyName} = ${m.model.serviceReference}.getByPrimaryKey(creds, id).map(_.map { model =>\n" ++
            s"      ${m.viewClass}(model, ${m.message})\n" +
            "    }.toSeq)"
        }
        val intFutures = intModels.map(_.model.propertyName).mkString(", ")
        val newContent = intFields.sorted.mkString("\n") + s"\n\n    val intSearches = Seq[Future[Seq[Html]]]($intFutures)"
        ExportHelper.replaceBetween(filename = filename, original = s, start = sStart, end = sEnd, newContent = newContent)
      }
    }

    def searchUuidFieldsFor(s: String) = {
      val uuidModels = markers.getOrElse("uuid-search", Nil).map(s => InjectSearchParams(config.getModel(s)))

      if (uuidModels.isEmpty) {
        s
      } else {
        val (sStart, sEnd) = "    // Start uuid searches" -> "    // End uuid searches"
        val uuidFields = uuidModels.map { m =>
          s"    val ${m.model.propertyName} = ${m.model.serviceReference}.getByPrimaryKey(creds, id).map(_.map { model =>\n" +
            s"      ${m.viewClass}(model, ${m.message})\n" +
            "    }.toSeq)"
        }
        val uuidFutures = uuidModels.map(_.model.propertyName).mkString(", ")
        val newContent = uuidFields.sorted.mkString("\n") + s"\n\n    val uuidSearches = Seq[Future[Seq[Html]]]($uuidFutures)"
        ExportHelper.replaceBetween(filename = filename, original = s, start = sStart, end = sEnd, newContent = newContent)
      }
    }

    val withStrings = searchStringFieldsFor(original)
    val withInts = searchIntFieldsFor(withStrings)
    searchUuidFieldsFor(withInts)
  }
}
