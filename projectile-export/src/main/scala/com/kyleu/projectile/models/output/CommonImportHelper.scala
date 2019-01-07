package com.kyleu.projectile.models.output

import com.kyleu.projectile.models.export.config.ExportConfiguration

object CommonImportHelper {
  def get(c: ExportConfiguration, s: String) = c.project.classOverrides.get(s) match {
    case Some(o) => o.substring(0, o.lastIndexOf('.')).trim.split('.').filter(_.nonEmpty).toSeq -> o.substring(o.lastIndexOf('.') + 1).trim
    case None => (s match {
      case "Application" => c.applicationPackage :+ "models"
      case "AuditHelper" => c.systemPackage ++ Seq("services", "audit")
      case "AuditRecordRowService" => c.applicationPackage ++ Seq("services", "audit")
      case "ApplicationDatabase" => c.systemPackage ++ Seq("services", "database")
      case "BaseController" => c.applicationPackage :+ "controllers"
      case "BaseQueries" => c.systemPackage ++ Seq("models", "queries")
      case "BaseResult" => c.resultsPackage
      case "CommonSchema" => c.systemPackage :+ "graphql"
      case "ControllerUtils" => c.systemPackage ++ Seq("web", "util")
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
      case "Filter" => c.resultsPackage :+ "filter"
      case "GraphQLContext" => c.systemPackage :+ "graphql"
      case "GraphQLQuery" => c.systemPackage :+ "graphql"
      case "GraphQLSchemaHelper" => c.systemPackage :+ "graphql"
      case "GraphQLUtils" => c.systemPackage :+ "graphql"
      case "Implicits" => Seq("scala", "concurrent", "ExecutionContext")
      case "JsonSerializers" => c.utilitiesPackage
      case "ModelServiceHelper" => c.systemPackage :+ "services"
      case "Note" => c.systemPackage ++ Seq("models", "note")
      case "NoteSchema" => c.systemPackage ++ Seq("models", "graphql", "note")
      case "ReftreeUtils" => c.systemPackage ++ Seq("web", "util")
      case "ServiceController" => c.applicationPackage ++ Seq("controllers", "admin")
      case "SlickQueryService" => c.systemPackage ++ Seq("services", "database", "slick")
      case "OrderBy" => c.resultsPackage :+ "orderBy"
      case "PagingOptions" => c.resultsPackage :+ "paging"
      case "RelationCount" => c.systemPackage ++ Seq("models", "result")
      case "ResultFieldHelper" => c.systemPackage ++ Seq("models", "queries")
      case "Row" => c.systemPackage ++ Seq("models", "database")
      case "Tag" => c.tagsPackage
      case "SystemUser" => c.systemPackage ++ Seq("models", "user")
      case "ThriftFutureUtils" => c.utilitiesPackage :+ "thrift"
      case "ThriftService" => c.utilitiesPackage :+ "thrift"
      case "ThriftServiceHelper" => c.utilitiesPackage :+ "thrift"
      case "ThriftServiceRegistry" => (c.systemPackage ++ c.project.getPackage(OutputPackage.Utils)) :+ "thrift"
      case "TraceData" => c.utilitiesPackage :+ "tracing"
      case "TracingService" => c.utilitiesPackage :+ "tracing"
      case _ => throw new IllegalStateException(s"No common import available with key [$s]")
    }) -> s
  }

  def getString(c: ExportConfiguration, s: String) = {
    val ret = get(c, s)
    (ret._1 :+ ret._2).mkString(".")
  }
}
