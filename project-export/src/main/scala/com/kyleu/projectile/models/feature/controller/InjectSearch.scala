package com.kyleu.projectile.models.feature.controller

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.feature.FeatureLogic
import com.kyleu.projectile.models.output.inject.{CommentProvider, TextSectionHelper}

object InjectSearch extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "SearchController.scala") {
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "controllers" :+ "admin" :+ "system"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: Seq[String]) = {
    val stringModels = markers.getOrElse("string-search", Nil).map { s =>
      InjectSearchParams(config, config.getModel(s, "search strings"))
    }.sortBy(_.model.className)
    val intModels = markers.getOrElse("int-search", Nil).map { s =>
      InjectSearchParams(config, config.getModel(s, "search ints"))
    }.sortBy(_.model.className)
    val uuidModels = markers.getOrElse("uuid-search", Nil).map { s =>
      InjectSearchParams(config, config.getModel(s, "search uuids"))
    }.sortBy(_.model.className)

    def searchStringFieldsFor(s: Seq[String]) = {
      val stringFields = stringModels.flatMap(m => Seq(
        s"val ${m.model.propertyName} = ${m.model.serviceReference}.searchExact(creds, q = q, limit = Some(5)).map(_.map { model =>",
        s"  ${m.viewClass}(model, ${m.message})",
        "})"
      ))
      val stringFutures = stringModels.map(_.model.propertyName).mkString(", ")
      val newLines = stringFields :+ "" :+ s"val stringSearches = Seq[Future[Seq[Html]]]($stringFutures)"

      val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "string searches")
      TextSectionHelper.replaceBetween(filename = filename, original = s, p = params, newLines = newLines, project = config.project.key)
    }

    def searchIntFieldsFor(s: Seq[String]) = {
      val intFields = intModels.flatMap(m => Seq(
        s"val ${m.model.propertyName} = ${m.model.serviceReference}.getByPrimaryKey(creds, id).map(_.map { model =>",
        s"  ${m.viewClass}(model, ${m.message})",
        "}.toSeq)"
      ))
      val intFutures = intModels.map(_.model.propertyName).mkString(", ")
      val newLines = intFields :+ "" :+ s"val intSearches = Seq[Future[Seq[Html]]]($intFutures)"

      val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "int searches")
      TextSectionHelper.replaceBetween(filename = filename, original = s, p = params, newLines = newLines, project = config.project.key)
    }

    def searchUuidFieldsFor(s: Seq[String]) = {
      val uuidFields = uuidModels.flatMap(m => Seq(
        s"val ${m.model.propertyName} = ${m.model.serviceReference}.getByPrimaryKey(creds, id).map(_.map { model =>",
        s"  ${m.viewClass}(model, ${m.message})",
        "}.toSeq)"
      ))
      val uuidFutures = uuidModels.map(_.model.propertyName).mkString(", ")
      val newLines = uuidFields :+ "" :+ s"val uuidSearches = Seq[Future[Seq[Html]]]($uuidFutures)"

      val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "uuid searches")
      TextSectionHelper.replaceBetween(filename = filename, original = s, p = params, newLines = newLines, project = config.project.key)
    }

    val withStrings = searchStringFieldsFor(original)
    val withInts = searchIntFieldsFor(withStrings)
    searchUuidFieldsFor(withInts)
  }
}
