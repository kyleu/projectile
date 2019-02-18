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

    file.addImport(Seq("scala", "concurrent"), "Future")
    val thriftService = svc.pkg.dropRight(1) :+ svc.key

    config.addCommonImport(file, "TraceData")
    config.addCommonImport(file, "ThriftFutureUtils", "toScalaFuture")
    config.addCommonImport(file, "ThriftService")
    config.addCommonImport(file, "ThriftServiceHelper")
    file.addImport(Seq("com", "twitter", "finagle"), "ThriftMux")
    file.addImport(Seq("com", "twitter", "scrooge"), "Request")

    val thriftServiceCanonicalName = thriftService.mkString(".")
    val thriftReqRepServicePerEndpointCanonicalName = s"$thriftServiceCanonicalName.ReqRepServicePerEndpoint"
    file.add(s"object ${svc.className} extends ThriftService(", 1)
    file.add(s"""key = "${svc.key}",""")
    file.add(s"""pkg = "${svc.pkg.mkString(".")}",""")
    file.add(s"""route = "/admin/thrift/${svc.propertyName.stripSuffix("Service")}"""")
    file.add(") {")

    file.add(s"""def mkServicePerEndpoint(url: String): $thriftReqRepServicePerEndpointCanonicalName = {""", 1)
    file.add(s"""ThriftMux.client.servicePerEndpoint[$thriftReqRepServicePerEndpointCanonicalName](url, "${svc.className}")""")
    file.add("}", -1)
    file.add(s"""def from(svc: $thriftReqRepServicePerEndpointCanonicalName): ${svc.className} = {""", 1)
    file.add(s"""new ${svc.className}(svc)""")
    file.add("}", -1)

    file.add(s"""case class Options(traceDataSerializer: Option[TraceData => Map[String, String]])""")
    file.add(s"""object Options {""", 1)
    file.add(s"""val default = Options(traceDataSerializer = None)""")
    file.add("}", -1)
    file.add("}", -1)
    file.add()
    file.add(s"""import ${svc.className}._""")
    file.add()
    file.add("@javax.inject.Singleton")
    file.add(s"""class ${svc.className} @javax.inject.Inject() (svc: $thriftServiceCanonicalName.ReqRepServicePerEndpoint, options: Options = Options.default) extends ThriftServiceHelper("${svc.key}") {""", 1)
    file.add(s"""private def injectTraceDataToHeaders(options: Options)(headers: Map[String, String], td: TraceData): Map[String, String] = {""", 1)
    file.add(s"""options.traceDataSerializer match {""", 1)
    file.add(s"""case Some(traceDataSerializer) => headers ++ traceDataSerializer(td)""")
    file.add(s"""case None => headers""")
    file.add("}", -1)
    file.add("}", -1)
    file.add(s"""def withTraceDataSerializer(traceDataSerializer: TraceData => Map[String, String]): ${svc.className} = {""", 1)
    file.add(s"""new ${svc.className}(svc, options.copy(traceDataSerializer = Some(traceDataSerializer)))""")
    file.add("}", -1)
    addMethods(thriftServiceCanonicalName, config, file, svc)
    file.add("}", -1)

    file
  }

  private[this] def addMethods(thriftServiceCanonicalName: String, config: ExportConfiguration, file: ScalaFile, svc: ExportService) = {
    svc.methods.foreach { method =>
      val args = method.args.map(a => ThriftFileHelper.declarationForField(config, a)).mkString(", ")
      method.args.foreach(a => a.addImport(config, file, svc.pkg))
      file.add()
      val s = FieldTypeAsScala.asScala(config, method.returnType)
      file.add(s"""def ${method.name}($args)(implicit parentTd: TraceData, headers: Map[String, String] = Map.empty): Future[$s] = trace("${method.name}") { td =>""", 1)
      val argsMapped = method.args.map(arg => ThriftMethodHelper.getArgCall(arg)).mkString(", ")
      FieldTypeImports.imports(config, method.returnType).foreach(pkg => file.addImport(pkg.init, pkg.lastOption.getOrElse(throw new IllegalStateException())))
      file.add(s"val _request = Request($thriftServiceCanonicalName.${method.name.capitalize}.Args($argsMapped))")
      file.add(s"val _requestWithHeaders = injectTraceDataToHeaders(options)(headers, td).foldLeft(_request)((acc, kv) => acc.setHeader(kv._1, kv._2))")
      file.add(s"val _response = svc.${method.name}(_requestWithHeaders)")
      file.add(s"_response.map(_.value)${ThriftMethodHelper.getReturnMapping(method.returnType)}")
      file.add("}", -1)
    }
  }
}
