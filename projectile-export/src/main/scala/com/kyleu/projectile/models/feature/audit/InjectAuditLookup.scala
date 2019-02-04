package com.kyleu.projectile.models.feature.audit

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.feature.{FeatureLogic, ModelFeature}
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.inject.{CommentProvider, TextSectionHelper}

object InjectAuditLookup extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "AuditLookup.scala") {
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "services" :+ "audit"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: Seq[String]) = {
    val newLines = config.models.filter(_.features(ModelFeature.Service)).filterNot(_.propertyName == "audit").filter(_.pkFields.nonEmpty).map { model =>
      val svc = model.injectedService(config)
      val pkArgs = model.pkFields.zipWithIndex.map(pkf => pkf._1.t match {
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
}
