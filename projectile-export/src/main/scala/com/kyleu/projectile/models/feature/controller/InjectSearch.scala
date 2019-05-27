package com.kyleu.projectile.models.feature.controller

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.feature.{FeatureLogic, ModelFeature}
import com.kyleu.projectile.models.feature.controller.db.twirl.TwirlHelper
import com.kyleu.projectile.models.output.inject.{CommentProvider, TextSectionHelper}

object InjectSearch extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "SearchHelper.scala") {
  override def applies(config: ExportConfiguration) = config.models.exists(_.features(ModelFeature.Controller))
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "models" :+ "search"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: Seq[String]) = {
    def searchStringFieldsFor(s: Seq[String]) = {
      val stringModels = markers.getOrElse("string-search", Nil).map { s =>
        InjectSearchParams(config, config.getModel(s, "search strings"))
      }.sortBy(_.model.className)
      val newLines = if (stringModels.isEmpty) { Nil } else {
        "Seq(" +: stringModels.map { m =>
          val comma = if (stringModels.lastOption.contains(m)) { "" } else { "," }
          val se = "searchExact(creds, q = q, limit = Some(5)"
          val cs = m.model.pkFields.map(f => "model." + f.propertyName)
          val route = s"${TwirlHelper.routesClass(config, m.model)}.view(${cs.mkString(", ")})"
          s"  ${m.model.injectedService(config)}.$se).map(_.map(model => $route -> ${m.viewClass}(model, ${m.message})))$comma"
        } :+ ") ++"
      }
      val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "string searches")
      TextSectionHelper.replaceBetween(filename = filename, original = s, p = params, newLines = newLines, project = config.project.key)
    }

    def searchIntFieldsFor(s: Seq[String]) = {
      val intModels = markers.getOrElse("int-search", Nil).map { s =>
        InjectSearchParams(config, config.getModel(s, "search ints"))
      }.sortBy(_.model.className)
      val newLines = if (intModels.isEmpty) { Nil } else {
        "Seq(" +: intModels.map { m =>
          val comma = if (intModels.lastOption.contains(m)) { "" } else { "," }
          val cs = m.model.pkFields.map(f => "model." + f.propertyName)
          val route = s"${TwirlHelper.routesClass(config, m.model)}.view(${cs.mkString(", ")})"
          s"  ${m.model.injectedService(config)}.getByPrimaryKey(creds, id).map(_.map(model => $route -> ${m.viewClass}(model, ${m.message})).toSeq)$comma"
        } :+ ") ++"
      }
      val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "int searches")
      TextSectionHelper.replaceBetween(filename = filename, original = s, p = params, newLines = newLines, project = config.project.key)
    }

    def searchUuidFieldsFor(s: Seq[String]) = {
      val uuidModels = markers.getOrElse("uuid-search", Nil).map { s =>
        InjectSearchParams(config, config.getModel(s, "search uuids"))
      }.sortBy(_.model.className)
      val newLines = if (uuidModels.isEmpty) { Nil } else {
        "Seq(" +: uuidModels.map { m =>
          val comma = if (uuidModels.lastOption.contains(m)) { "" } else { "," }
          val cs = m.model.pkFields.map(f => "model." + f.propertyName)
          val route = s"${TwirlHelper.routesClass(config, m.model)}.view(${cs.mkString(", ")})"
          s"  ${m.model.injectedService(config)}.getByPrimaryKey(creds, id).map(_.map(model => $route -> ${m.viewClass}(model, ${m.message})).toSeq)$comma"
        } :+ ") ++"
      }
      val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "uuid searches")
      TextSectionHelper.replaceBetween(filename = filename, original = s, p = params, newLines = newLines, project = config.project.key)
    }

    val withStrings = searchStringFieldsFor(original)
    val withInts = searchIntFieldsFor(withStrings)
    searchUuidFieldsFor(withInts)
  }
}
