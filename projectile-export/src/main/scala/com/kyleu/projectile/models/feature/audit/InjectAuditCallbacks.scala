package com.kyleu.projectile.models.feature.audit

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.feature.controller.db.twirl.TwirlHelper
import com.kyleu.projectile.models.feature.{FeatureLogic, ModelFeature}
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.inject.{CommentProvider, TextSectionHelper}

object InjectAuditCallbacks extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "AuditCallbacks.scala") {
  override def applies(config: ExportConfiguration) = config.models.exists(_.features(ModelFeature.Service))
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "services" :+ "audit"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[(String, String)]], original: Seq[String]) = {
    if (original.exists(_.contains("registry lookups"))) {
      routesLogic(config, markers, lookupLogic(config, markers, original))
    } else {
      routesLogic(config, markers, original)
    }
  }

  private[this] def lookupLogic(config: ExportConfiguration, markers: Map[String, Seq[(String, String)]], original: Seq[String]) = {
    val newLines = config.models.filter(_.features(ModelFeature.Service)).filterNot(_.propertyName == "audit").filter(_.pkFields.nonEmpty).map { model =>
      val svc = model.injectedService(config)
      val pkArgs = model.pkFields.zipWithIndex.map(pkf => pkf._1.t match {
        case FieldType.SerialType => s"longArg(arg(${pkf._2}))"
        case FieldType.EnumType(key) =>
          val e = config.getEnumOpt(key).getOrElse(throw new IllegalStateException("Cannot load enum"))
          s"enumArg(${e.fullClassPath(config)})(arg(${pkf._2}))"
        case _ => s"${pkf._1.t.value}Arg(arg(${pkf._2}))"
      }).mkString(", ")

      s"""case "${model.propertyName.toLowerCase}" => $svc.getByPrimaryKey(creds, $pkArgs)"""
    }.sorted

    val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "registry lookups")
    TextSectionHelper.replaceBetween(filename = filename, original = original, p = params, newLines = newLines, project = config.project.key)
  }

  private[this] def routesLogic(config: ExportConfiguration, markers: Map[String, Seq[(String, String)]], original: Seq[String]) = {
    val newLines = config.models.filter(_.features(ModelFeature.Controller)).filter(_.pkFields.nonEmpty).map { model =>
      val pkArgs = model.pkFields.zipWithIndex.map(pkf => pkf._1.t match {
        case FieldType.SerialType => s"longArg(arg(${pkf._2}))"
        case FieldType.EnumType(key) =>
          val cn = config.getEnumOpt(key).getOrElse(throw new IllegalStateException("Cannot load enum")).className
          s"enumArg($cn)(arg(${pkf._2}))"
        case _ => s"${pkf._1.t.value}Arg(arg(${pkf._2}))"
      }).mkString(", ")

      s"""case "${model.propertyName.toLowerCase}" => ${TwirlHelper.routesClass(config, model)}.view($pkArgs)"""
    }.sorted

    val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "audit calls")
    TextSectionHelper.replaceBetween(filename = filename, original = original, p = params, newLines = newLines, project = config.project.key)
  }
}
