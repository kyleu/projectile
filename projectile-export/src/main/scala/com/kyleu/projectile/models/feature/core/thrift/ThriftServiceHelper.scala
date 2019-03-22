package com.kyleu.projectile.models.feature.core.thrift

import com.kyleu.projectile.models.export.ExportService
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.file.ScalaFile

object ThriftServiceHelper {
  def addImports(config: ExportConfiguration, file: ScalaFile) = {
    file.addImport(Seq("scala", "concurrent"), "Future")

    config.addCommonImport(file, "TraceData")
    config.addCommonImport(file, "TracingService")

    config.addCommonImport(file, "ThriftFutureUtils", "toScalaFuture")
    config.addCommonImport(file, "ThriftService")
    file.addImport(Seq("com", "twitter", "finagle"), "ThriftMux")
    file.addImport(Seq("com", "twitter", "scrooge"), "Request")
  }

  def addJavadoc(file: ScalaFile, svc: ExportService) = {
    val classCanonicalName = (svc.pkg :+ svc.className).mkString(".")
    file.add("/***")
    file.add(" * {{{")
    file.add(s" * val svcPerEndpoint = $classCanonicalName.mkServicePerEndpoint(url)")
    file.add(s" * val myService = $classCanonicalName")
    file.add(" *   .from(svcPerEndpoint)")
    file.add(" *   .withTracingService(openTracingTracingService)")
    file.add(" *   .withThriftSpanNamePrefix(\"myThriftService\")")
    file.add(" * }}}")
    file.add(" */")
  }

  def addHelperMethods(svc: ExportService, file: ScalaFile) = {
    file.add(s"""private def injectTraceDataToHeaders(options: Options)(headers: Map[String, String], td: TraceData): Map[String, String] = {""", 1)
    file.add(s"""options.traceDataSerializer match {""", 1)
    file.add(s"""case Some(traceDataSerializer) => headers ++ traceDataSerializer(td)""")
    file.add(s"""case None => headers""")
    file.add("}", -1)
    file.add("}", -1)

    file.add(s"""private def trace[A](traceName: String)(f: TraceData => Future[A])(implicit parentData: TraceData): Future[A] = {""", 1)
    file.add(s"""options.tracingService.map { tracingService =>""", 1)
    file.add(s"""tracingService.trace(s"$${options.thriftSpanNamePrefix}.$$traceName")(f)""")
    file.add("}.getOrElse(f(parentData))", -1)
    file.add("}", -1)

    file.add(s"""def withTracingService(tracingService: TracingService): ${svc.className} = {""", 1)
    file.add(s"""new ${svc.className}(svc, options.copy(tracingService = Some(tracingService)))""")
    file.add("}", -1)
    file.add(s"""def withThriftSpanNamePrefix(prefix: String): ${svc.className} = {""", 1)
    file.add(s"""new ${svc.className}(svc, options.copy(thriftSpanNamePrefix = prefix))""")
    file.add("}", -1)

    file.add(s"""def withTraceDataSerializer(traceDataSerializer: TraceData => Map[String, String]): ${svc.className} = {""", 1)
    file.add(s"""new ${svc.className}(svc, options.copy(traceDataSerializer = Some(traceDataSerializer)))""")
    file.add("}", -1)
    file.add(s"""def healthcheck(implicit td: TraceData): Future[String] = {""", 1)
    file.add(s"""Future.successful("${svc.className}: OK")""")
    file.add("}", -1)
  }
}
