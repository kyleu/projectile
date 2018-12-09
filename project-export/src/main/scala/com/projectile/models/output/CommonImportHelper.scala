package com.projectile.models.output

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.file.OutputFile

object CommonImportHelper {
  def get(c: ExportConfiguration, f: OutputFile, s: String) = c.project.classOverrides.get(s) match {
    case Some(o) => o.substring(0, o.lastIndexOf('.')).trim.split('.').filter(_.nonEmpty).toSeq -> o.substring(o.lastIndexOf('.') + 1).trim
    case None => (s match {
      case "Application" => c.applicationPackage :+ "models"
      case "AuditHelper" => c.systemPackage ++ Seq("services", "audit")
      case "AuditRecordService" => c.applicationPackage ++ Seq("services", "audit")
      case "ApplicationDatabase" => c.systemPackage ++ Seq("services", "database")
      case "BaseController" => c.applicationPackage :+ "controllers"
      case "BaseQueries" => c.systemPackage ++ Seq("models", "queries")
      case "BaseResult" => c.resultsPackage
      case "CommonSchema" => c.systemPackage :+ "graphql"
      case "ControllerUtils" => c.utilitiesPackage :+ "web"
      case "Credentials" => c.applicationPackage ++ Seq("models", "auth")
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
      case "Filter" => c.resultsPackage :+ "filter"
      case "FutureUtils" => c.utilitiesPackage
      case "GraphQLContext" => c.applicationPackage :+ "graphql"
      case "GraphQLQuery" => c.applicationPackage :+ "graphql"
      case "GraphQLSchemaHelper" => c.applicationPackage :+ "graphql"
      case "GraphQLUtils" => c.systemPackage :+ "graphql"
      case "JsonSerializers" => c.utilitiesPackage
      case "ModelServiceHelper" => c.systemPackage :+ "services"
      case "NoteSchema" => c.applicationPackage ++ Seq("models", "note")
      case "ReftreeUtils" => c.utilitiesPackage
      case "ServiceController" => c.applicationPackage ++ Seq("controllers", "admin")
      case "SlickQueryService" => c.systemPackage ++ Seq("services", "database", "slick")
      case "OrderBy" => c.resultsPackage :+ "orderBy"
      case "PagingOptions" => c.resultsPackage :+ "paging"
      case "RelationCount" => c.systemPackage ++ Seq("models", "result")
      case "ResultFieldHelper" => c.systemPackage ++ Seq("models", "queries")
      case "Row" => c.systemPackage ++ Seq("models", "database")
      case "Tag" => c.tagsPackage
      case "SystemUser" => c.applicationPackage ++ Seq("models", "user")
      case "ThriftFutureUtils" => c.utilitiesPackage :+ "thrift"
      case "ThriftService" => c.utilitiesPackage :+ "thrift"
      case "ThriftServiceHelper" => c.utilitiesPackage :+ "thrift"
      case "ThriftServiceRegistry" => c.utilitiesPackage :+ "thrift"
      case "TraceData" => c.utilitiesPackage :+ "tracing"
      case "TracingService" => c.utilitiesPackage :+ "tracing"
      case _ => throw new IllegalStateException(s"No common import available with key [$s]")
    }) -> s
  }
}
