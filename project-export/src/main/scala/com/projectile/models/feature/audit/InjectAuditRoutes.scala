package com.projectile.models.feature.audit

import com.projectile.models.export.FieldType
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.controller.twirl.TwirlHelper
import com.projectile.models.feature.{FeatureLogic, ModelFeature}
import com.projectile.models.output.{ExportHelper, OutputPath}

object InjectAuditRoutes extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "AuditRoutes.scala") {
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "services" :+ "audit"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: String) = {
    val newContent = config.models.filter(_.features(ModelFeature.Controller)).filter(_.pkFields.nonEmpty).map { model =>
      val pkArgs = model.pkFields.zipWithIndex.map(pkf => pkf._1.t match {
        case FieldType.EnumType =>
          val cn = pkf._1.enumOpt(config).getOrElse(throw new IllegalStateException("Cannot load enum.")).className
          s"enumArg($cn)(arg(${pkf._2}))"
        case _ => s"${pkf._1.t.value}Arg(arg(${pkf._2}))"
      }).mkString(", ")

      s"""    case "${model.propertyName.toLowerCase}" => ${TwirlHelper.routesClass(config, model)}.view($pkArgs)"""
    }.sorted.mkString("\n")

    ExportHelper.replaceBetween(
      filename = filename, original = original, start = "    /* Start audit calls */", end = "    /* End audit calls */", newContent = newContent
    )
  }
}
