package com.kyleu.projectile.models.feature.controller.db

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.{ExportEnum, ExportModel, ExportModelReference}
import com.kyleu.projectile.models.feature.{EnumFeature, ModelFeature, ProjectFeature}
import com.kyleu.projectile.models.output.file.RoutesFile

object RoutesFiles {
  private[this] val limitArgs = "limit: Option[Int] ?= None, offset: Option[Int] ?= None, t: Option[String] ?= None"
  private[this] val listArgs = "q: Option[String] ?= None, orderBy: Option[String] ?= None, orderAsc: Boolean ?= true, " + limitArgs
  private[this] val autocompleteArgs = "q: Option[String] ?= None, orderBy: Option[String] ?= None, orderAsc: Boolean ?= true, limit: Option[Int] ?= None"
  private[this] val relationArgs = s"orderBy: Option[String] ?= None, orderAsc: Boolean ?= true, $limitArgs, embedded: Boolean ?= false"

  def export(config: ExportConfiguration) = {
    val filtered = config.models.filter(_.features(ModelFeature.Controller)).filter(_.inputType.isDatabase)

    val packages = filtered.flatMap(_.pkg.headOption).distinct

    val testRoute = if ((!packages.contains("test")) && config.project.features(ProjectFeature.Tests)) { Seq("test") } else { Nil }

    val routesContent = (packages ++ testRoute).map { p =>
      val ms = config.models.filter(_.features(ModelFeature.Controller)).filter(_.pkg.headOption.contains(p))
      val es = config.enums.filter(_.features(EnumFeature.Controller)).filter(_.pkg.headOption.contains(p))
      val solo = es.isEmpty && ms.size == 1

      val extra = if (p == "test" && config.project.features(ProjectFeature.Tests)) {
        if (solo) {
          Seq(s"GET   /test   ${(config.applicationPackage :+ "controllers" :+ "admin").mkString(".")}.test.TestController.test()")
        } else {
          Seq(s"GET   /       ${(config.applicationPackage :+ "controllers" :+ "admin").mkString(".")}.test.TestController.test()")
        }
      } else {
        Nil
      }

      if (solo) {
        p -> (routesContentFor(config, ms.headOption.getOrElse(throw new IllegalStateException()), solo = true) ++ extra)
      } else {
        p -> (ms.flatMap(m => routesContentFor(config, m)) ++ es.flatMap(e => enumRoutesContentFor(config, e)) ++ extra)
      }
    }

    routesContent.map { p =>
      val f = RoutesFile(p._1)
      p._2.foreach(l => f.add(l))
      f
    }
  }

  def routesContentFor(config: ExportConfiguration, model: ExportModel, solo: Boolean = false) = {
    val controllerClass = (model.controllerPackage(config) :+ (model.className + "Controller")).mkString(".")
    val wsLength = 56
    val prefix = if (solo) { "" } else { s"/${model.propertyName}" }
    val root = if (solo) { "/" } else { s"/${model.propertyName}" }
    val pkArgs = model.pkFields.map(x => s"${x.propertyName}: ${x.scalaTypeFull(config).mkString(".")}").mkString(", ")
    val urlArgs = model.pkFields.map(f => ":" + f.propertyName).mkString("/")

    val comment = s"# ${model.title} Routes"
    val listWs = (0 until (wsLength - root.length)).map(_ => " ").mkString
    val list = s"GET         $root $listWs $controllerClass.list($listArgs)"
    val autocomplete = s"GET         $prefix/autocomplete ${listWs.drop(13)} $controllerClass.autocomplete($autocompleteArgs)"
    val createForm = s"GET         $prefix/form ${listWs.drop(5)} $controllerClass.createForm"
    val createAct = s"POST        $root $listWs $controllerClass.create"
    val bulkEditAct = if (model.foreignKeys.isEmpty || model.pkFields.isEmpty) {
      Nil
    } else {
      Seq(s"POST        $root/bulk ${listWs.drop(5)} $controllerClass.bulkEdit")
    }
    val detail = if (model.pkFields.isEmpty) {
      Nil
    } else {
      val detailUrl = prefix + "/" + urlArgs
      val detailWs = (0 until (wsLength - detailUrl.length)).map(_ => " ").mkString

      val view = s"GET         $detailUrl $detailWs $controllerClass.view($pkArgs, t: Option[String] ?= None)"
      val counts = if (ExportModelReference.validReferences(config, model).isEmpty) {
        Nil
      } else {
        Seq(s"GET         $detailUrl/counts ${detailWs.drop(7)} $controllerClass.relationCounts($pkArgs)")
      }
      val extras = Seq(
        s"GET         $detailUrl/form ${detailWs.drop(5)} $controllerClass.editForm($pkArgs)",
        s"POST        $detailUrl $detailWs $controllerClass.edit($pkArgs)",
        s"GET         $detailUrl/remove ${detailWs.drop(7)} $controllerClass.remove($pkArgs)"
      )
      view +: (counts ++ extras)
    }
    val fks = model.foreignKeys.flatMap { fk =>
      fk.references match {
        case h :: Nil =>
          val col = model.getField(h.source)
          val url = prefix + s"/by${col.className}/:${col.propertyName}"
          val ws = (0 until (wsLength - url.length)).map(_ => " ").mkString
          val t = col.scalaTypeFull(config).mkString(".")
          Seq(
            s"GET         $url $ws $controllerClass.by${col.className}(${col.propertyName}: $t, $relationArgs)",
            s"GET         $url/bulk ${ws.drop(5)} $controllerClass.by${col.className}BulkForm(${col.propertyName}: $t)"
          )
        case _ => Nil
      }
    }
    "" +: comment +: list +: autocomplete +: createForm +: createAct +: (bulkEditAct ++ fks ++ detail)
  }

  private[this] def enumRoutesContentFor(config: ExportConfiguration, e: ExportEnum) = {
    val controllerClass = (e.controllerPackage(config) :+ (e.className + "Controller")).mkString(".")
    val detailWs = (0 until (55 - e.propertyName.length)).map(_ => " ").mkString
    Seq(s"GET         /${e.propertyName} $detailWs $controllerClass.list()", "")
  }
}
