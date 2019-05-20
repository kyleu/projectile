package com.kyleu.projectile.models.output

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.util.StringUtils

object CommonImportHelper {
  def get(c: ExportConfiguration, s: String) = c.project.classOverrides.get(s) match {
    case Some(o) => StringUtils.toList(o.substring(0, o.lastIndexOf('.')).trim, '.') -> o.substring(o.lastIndexOf('.') + 1).trim
    case None => (s match {
      case "AugmentService" => c.systemPackage ++ Seq("services", "augment")
      case "Application" => c.systemPackage :+ "models" :+ "module"
      case "AuditHelper" => c.systemPackage ++ Seq("services", "audit")
      case "AuditService" => c.systemPackage ++ Seq("services", "audit")
      case "AuthController" => c.systemPackage :+ "controllers"
      case "BaseController" => c.systemPackage :+ "controllers"
      case "BaseQueries" => c.systemPackage ++ Seq("models", "queries")
      case "BaseResult" => c.resultsPackage
      case "CommonSchema" => c.systemPackage :+ "graphql"
      case "ControllerUtils" => c.systemPackage ++ Seq("models", "web")
      case "Credentials" => c.systemPackage ++ Seq("services")
      case "CsvUtils" => c.utilitiesPackage
      case "DatabaseField" => c.systemPackage ++ Seq("models", "database")
      case "DatabaseFieldType" => c.systemPackage ++ Seq("models", "database")
      case "DataField" => c.resultsPackage :+ "data"
      case "DataSummary" => c.resultsPackage :+ "data"
      case "DataFieldModel" => c.resultsPackage :+ "data"
      case "DateUtils" => c.utilitiesPackage
      case "DoobieQueries" => c.systemPackage ++ Seq("services", "database", "doobie")
      case "DoobieQueryService" => c.systemPackage ++ Seq("services", "database", "doobie")
      case "DoobieTestHelper" => c.systemPackage ++ Seq("services", "database", "doobie")
      case "ExecutionContext" => Seq("scala", "concurrent")
      case "Filter" => c.resultsPackage :+ "filter"
      case "GraphQLContext" => c.systemPackage :+ "graphql"
      case "GraphQLQuery" => c.systemPackage :+ "graphql"
      case "GraphQLSchemaHelper" => c.systemPackage :+ "graphql"
      case "GraphQLUtils" => c.systemPackage :+ "graphql"
      case "JdbcDatabase" => c.systemPackage ++ Seq("services", "database")
      case "JsonSerializers" => c.utilitiesPackage
      case "ModelAuthServiceHelper" => c.systemPackage :+ "services"
      case "ModelServiceHelper" => c.systemPackage :+ "services"
      case "Note" => c.systemPackage ++ Seq("models", "note")
      case "NoteSchema" => c.systemPackage ++ Seq("models", "graphql", "note")
      case "NoteService" => c.systemPackage :+ "services" :+ "note"
      case "NullUtils" => c.utilitiesPackage
      case "OrderBy" => c.resultsPackage :+ "orderBy"
      case "PagingOptions" => c.resultsPackage :+ "paging"
      case "RelationCount" => c.systemPackage ++ Seq("models", "result")
      case "ReftreeUtils" => c.systemPackage ++ Seq("models", "web")
      case "ResultFieldHelper" => c.systemPackage ++ Seq("models", "queries")
      case "Row" => c.systemPackage ++ Seq("models", "database")
      case "ServiceAuthController" => c.systemPackage :+ "controllers"
      case "ServiceController" => c.systemPackage :+ "controllers"
      case "SlickQueryService" => c.systemPackage ++ Seq("services", "database", "slick")
      case "SystemUser" => c.systemPackage ++ Seq("models", "user")
      case "Tag" => c.tagsPackage
      case "TextQuery" => c.resultsPackage
      case "ThriftFutureUtils" => c.utilitiesPackage :+ "thrift"
      case "ThriftService" => c.utilitiesPackage :+ "thrift"
      case "ThriftServiceHelper" => c.utilitiesPackage :+ "thrift"
      case "ThriftServiceRegistry" => c.applicationPackage :+ "util" :+ "thrift"
      case "TraceData" => c.utilitiesPackage :+ "tracing"
      case "TracingService" => c.utilitiesPackage :+ "tracing"
      case "UiConfig" => c.systemPackage :+ "models" :+ "config"
      case _ => throw new IllegalStateException(s"No common import available with key [$s]")
    }) -> s
  }

  def getString(c: ExportConfiguration, s: String) = {
    val ret = get(c, s)
    (ret._1 :+ ret._2).mkString(".")
  }
}
