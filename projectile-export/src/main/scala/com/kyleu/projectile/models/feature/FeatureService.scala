package com.kyleu.projectile.models.feature

import better.files.File
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.ProjectFeature._
import com.kyleu.projectile.models.feature.audit.AuditLogic
import com.kyleu.projectile.models.feature.controller.ControllerLogic
import com.kyleu.projectile.models.feature.core.CoreLogic
import com.kyleu.projectile.models.feature.datamodel.DataModelLogic
import com.kyleu.projectile.models.feature.doobie.DoobieLogic
import com.kyleu.projectile.models.feature.graphql.GraphQLLogic
import com.kyleu.projectile.models.feature.openapi.OpenApiLogic
import com.kyleu.projectile.models.feature.service.ServiceLogic
import com.kyleu.projectile.models.feature.slick.SlickLogic
import com.kyleu.projectile.models.feature.thrift.ThriftLogic
import com.kyleu.projectile.models.feature.wiki.WikiLogic
import com.kyleu.projectile.models.output.OutputLog

object FeatureService {
  def getLogic(feature: ProjectFeature) = feature match {
    case Core => Some(CoreLogic)
    case DataModel => Some(DataModelLogic)
    case ScalaJS => None
    case Slick => Some(SlickLogic)
    case Doobie => Some(DoobieLogic)
    case Service => Some(ServiceLogic)
    case GraphQL => Some(GraphQLLogic)
    case Controller => Some(ControllerLogic)
    case Audit => Some(AuditLogic)
    case Notes => None
    case Tests => None
    case Thrift => Some(ThriftLogic)
    case OpenAPI => Some(OpenApiLogic)
    case Wiki => Some(WikiLogic)
  }

  def export(feature: ProjectFeature, projectRoot: File, config: ExportConfiguration, verbose: Boolean) = {
    val startMs = System.currentTimeMillis

    val logs = collection.mutable.ArrayBuffer.empty[OutputLog]

    def info(s: String) = {
      logs += OutputLog(s, System.currentTimeMillis - startMs)
    }
    def debug(s: String) = if (verbose) { info(s) }

    val logic = getLogic(feature)

    val files = logic.map(_.export(config = config, info = info, debug = debug)).getOrElse(Nil)
    val markers = files.map(_.markers).foldLeft(Map.empty[String, Seq[(String, String)]]) { (l, r) =>
      (l.keys.toSeq ++ r.keys.toSeq).distinct.map(k => k -> (l.getOrElse(k, Nil) ++ r.getOrElse(k, Nil))).toMap
    }
    val injections = logic.map(_.inject(config = config, projectRoot = projectRoot, markers = markers, info = info, debug = debug)).getOrElse(Nil)
    val duration = System.currentTimeMillis - startMs
    info(s"Feature [${feature.title}] produced [${files.length}] files in [${duration}ms]")
    FeatureOutput(feature = feature, files = files, injections = injections, logs = logs.toIndexedSeq, duration = duration)
  }

}
