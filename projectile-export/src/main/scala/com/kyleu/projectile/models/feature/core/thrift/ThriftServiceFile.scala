package com.kyleu.projectile.models.feature.core.thrift

import com.kyleu.projectile.models.export.ExportService
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.{FieldTypeAsScala, FieldTypeImports}
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.thrift.input.{ThriftFileHelper, ThriftMethodHelper}

object ThriftServiceFile {

  def export(config: ExportConfiguration, svc: ExportService) = {
    val file = ScalaFile(path = OutputPath.ServerSource, dir = svc.pkg, key = svc.className)
    val className = svc.className
    addImports(config, file)
    addJavadoc(file, svc)
    addCompanionObject(svc, file)
    file.add("@javax.inject.Singleton")
    val constructorArguments = Seq(
      s"svc: ${getThriftReqRepServicePerEndpointCanonicalName(svc)}",
      s"options: $className.Options = $className.Options.default"
    ).mkString(", ")
    file.add(s"""class $className @javax.inject.Inject() ($constructorArguments) {""", 1)
    addHelperMethods(svc, file)
    addMethods(config, file, svc)
    file.add("}", -1)
    file
  }

  private def addImports(
    config: ExportConfiguration,
    file: ScalaFile
  ) = {
    file.addImport(Seq("scala", "concurrent"), "Future")

    config.addCommonImport(file, "TraceData")
    config.addCommonImport(file, "TracingService")

    config.addCommonImport(file, "ThriftFutureUtils", "toScalaFuture")
    config.addCommonImport(file, "ThriftService")
    file.addImport(Seq("com", "twitter", "finagle"), "ThriftMux")
    file.addImport(Seq("com", "twitter", "scrooge"), "Request")
  }
  private def addCompanionObject(svc: ExportService, file: ScalaFile) = {
    val thriftReqRepServicePerEndpointCanonicalName = getThriftReqRepServicePerEndpointCanonicalName(svc)
    file.add(s"object ${svc.className} extends ThriftService(", 1)
    file.add(s"""key = "${svc.key}",""")
    file.add(s"""pkg = "${svc.pkg.mkString(".")}",""")
    file.add(s"""route = "/admin/thrift/${svc.propertyName.stripSuffix("Service")}"""")
    file.add(") {", -1)
    file.indent(1)
    file.add(s"""def mkServicePerEndpoint(url: String): $thriftReqRepServicePerEndpointCanonicalName = {""", 1)
    file.add(s"""ThriftMux.client.servicePerEndpoint[$thriftReqRepServicePerEndpointCanonicalName](url, "${svc.className}")""")
    file.add("}", -1)
    file.add(s"""def from(svc: $thriftReqRepServicePerEndpointCanonicalName): ${svc.className} = {""", 1)
    file.add(s"""new ${svc.className}(svc)""")
    file.add("}", -1)

    file.add(s"""case class Options(""", 1)
    file.add(s"""tracingService: Option[TracingService],""")
    file.add(s"""traceDataSerializer: Option[TraceData => Map[String, String]],""")
    file.add(s"""thriftSpanNamePrefix: String""")
    file.add(s""")""", -1)
    file.add(s"""object Options {""", 1)
    file.add(s"""val default = Options(tracingService = None, traceDataSerializer = None, thriftSpanNamePrefix = s"thrift.${svc.className}.")""")
    file.add("}", -1)
    file.add("}", -1)
    file.add()
    file.add(s"""import ${svc.className}._""")
    file.add()
  }
  private def addJavadoc(file: ScalaFile, svc: ExportService) = {
    val classCanonicalName = (svc.pkg :+ svc.className).mkString(".")
    file.add(s"""
                |/***
                |* {{{
                |* val svcPerEndpoint = $classCanonicalName.mkServicePerEndpoint(url)
                |* val myService = $classCanonicalName
                |*   .from(svcPerEndpoint)
                |*   .withTracingService(openTracingTracingService)
                |*   .withThriftSpanNamePrefix("myThriftService")
                |*   .withTraceDataSerializer(td => Map.empty)
                |* }}}
                |*/
       """.stripMargin)
  }
  private def addHelperMethods(
    svc: ExportService,
    file: ScalaFile
  ) = {
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

  private[this] def addMethods(config: ExportConfiguration, file: ScalaFile, svc: ExportService) = {
    val thriftServiceCanonicalName = getThriftServiceClassCanonicalName(svc)
    svc.methods.foreach { method =>
      val args = method.args.map(a => ThriftFileHelper.declarationForField(config, a)).mkString(", ")
      val implicitArgs = s"implicit parentTd: TraceData, headers: Map[String, String] = Map.empty"
      method.args.foreach(a => a.addImport(config, file, svc.pkg))
      file.add()
      val s = FieldTypeAsScala.asScala(config, method.returnType)
      file.add(s"""def ${method.name}($args)(${implicitArgs}): Future[$s] = trace("${method.name}") { td =>""", 1)
      val argsMapped = method.args.map(arg => ThriftMethodHelper.getArgCall(arg)).mkString(", ")
      FieldTypeImports.imports(config, method.returnType).foreach(pkg => file.addImport(pkg.init, pkg.lastOption.getOrElse(throw new IllegalStateException())))
      file.add(s"val _request = Request($thriftServiceCanonicalName.${method.name.capitalize}.Args($argsMapped))")
      file.add(s"val _requestWithHeaders = injectTraceDataToHeaders(options)(headers, td).foldLeft(_request)((acc, kv) => acc.setHeader(kv._1, kv._2))")
      file.add(s"val _response = svc.${method.name}(_requestWithHeaders)")
      file.add(s"_response.map(_.value)${ThriftMethodHelper.getReturnMapping(method.returnType)}")
      file.add("}", -1)
    }
  }

  private def getThriftServiceClass(svc: ExportService): Seq[String] = {
    svc.pkg.dropRight(1) :+ svc.key
  }
  private def getThriftServiceClassCanonicalName(svc: ExportService): String = {
    getThriftServiceClass(svc).mkString(".")
  }
  private def getThriftReqRepServicePerEndpointCanonicalName(svc: ExportService): String = {
    s"${getThriftServiceClassCanonicalName(svc)}.ReqRepServicePerEndpoint"
  }
}
