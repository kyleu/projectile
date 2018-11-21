package com.projectile.models.output

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.file.OutputFile

object CommonImportHelper {
  def get(c: ExportConfiguration, f: OutputFile, s: String) = c.project.classOverrides.get(s) match {
    case Some(o) => o.substring(0, o.lastIndexOf('.')).trim.split('.').filter(_.nonEmpty).toSeq -> o.substring(o.lastIndexOf('.') + 1).trim
    case None => (s match {
      case "Application" => c.applicationPackage :+ "models"
      case "AuditRecordService" => c.applicationPackage ++ Seq("services", "audit")
      case "BaseController" => c.applicationPackage :+ "controllers"
      case "BaseResult" => c.resultsPackage
      case "CommonSchema" => c.systemPackage :+ "graphql"
      case "Credentials" => c.applicationPackage ++ Seq("models", "auth")
      case "DateUtils" => c.utilitiesPackage
      case "DoobieQueries" => c.systemPackage ++ Seq("services", "database", "doobie")
      case "Filter" => c.resultsPackage :+ "filter"
      case "GraphQLContext" => c.applicationPackage :+ "graphql"
      case "GraphQLSchemaHelper" => c.applicationPackage :+ "graphql"
      case "GraphQLUtils" => c.systemPackage :+ "graphql"
      case "JsonSerializers" => c.utilitiesPackage
      case "ReftreeUtils" => c.utilitiesPackage
      case "ServiceController" => c.applicationPackage ++ Seq("controllers", "admin")
      case "OrderBy" => c.resultsPackage :+ "orderBy"
      case "PagingOptions" => c.resultsPackage :+ "paging"
      case "RelationCount" => c.systemPackage ++ Seq("models", "result")
      case "ResultFieldHelper" => c.systemPackage ++ Seq("models", "queries")
      case "Tag" => c.tagsPackage
      case "TraceData" => c.utilitiesPackage :+ "tracing"
      case "TracingService" => c.utilitiesPackage :+ "tracing"
      case _ => throw new IllegalStateException(s"No common import available with key [$s]")
    }) -> s
  }
}
