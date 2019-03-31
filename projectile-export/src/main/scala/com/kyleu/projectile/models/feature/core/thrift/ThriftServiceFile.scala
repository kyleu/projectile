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
    ThriftServiceHelper.addImports(config, file)
    ThriftServiceHelper.addJavadoc(file, svc)
    addCompanionObject(svc, file)
    file.add("@javax.inject.Singleton")
    val constructorArguments = Seq(
      s"svc: ${getThriftReqRepServicePerEndpointCanonicalName(svc)}",
      s"options: $className.Options = $className.Options.default"
    ).mkString(", ")
    file.add(s"""class $className @javax.inject.Inject() ($constructorArguments) {""", 1)
    ThriftServiceHelper.addHelperMethods(svc, file)
    addMethods(config, file, svc)
    file.add("}", -1)
    file
  }

  private[this] def addCompanionObject(svc: ExportService, file: ScalaFile) = {
    val thriftReqRepServicePerEndpointCanonicalName = getThriftReqRepServicePerEndpointCanonicalName(svc)
    val rt = s"""route = "/admin/thrift/${svc.propertyName.stripSuffix("Service")}""""
    file.add(s"""object ${svc.className} extends ThriftService(key = "${svc.key}", pkg = "${svc.pkg.mkString(".")}", $rt) {""", 1)
    file.add(s"""def mkServicePerEndpoint(url: String): $thriftReqRepServicePerEndpointCanonicalName = {""", 1)
    file.add(s"""ThriftMux.client.servicePerEndpoint[$thriftReqRepServicePerEndpointCanonicalName](url, "${svc.className}")""")
    file.add("}", -1)
    file.add(s"""def from(svc: $thriftReqRepServicePerEndpointCanonicalName) = new ${svc.className}(svc)""")
    val tds = s"traceDataSerializer: Option[TraceData => Map[String, String]]"
    file.add(s"""case class Options(tracingService: Option[TracingService], $tds, thriftSpanNamePrefix: String)""")
    file.add(s"""object Options {""", 1)
    file.add(s"""val default = Options(tracingService = None, traceDataSerializer = None, thriftSpanNamePrefix = s"thrift.${svc.className}.")""")
    file.add("}", -1)
    file.add("}", -1)
    file.add()
    file.add(s"""import ${svc.className}._""")
    file.add()
  }

  private[this] def addMethods(config: ExportConfiguration, file: ScalaFile, svc: ExportService) = {
    val thriftServiceCanonicalName = getThriftServiceClassCanonicalName(svc)
    svc.methods.foreach { method =>
      val args = method.args.map(a => ThriftFileHelper.declarationForField(config, a)).mkString(", ")
      val implicitArgs = s"implicit parentTd: TraceData, headers: Map[String, String] = Map.empty"
      method.args.foreach(a => a.addImport(config, file, svc.pkg))
      file.add()
      val s = FieldTypeAsScala.asScala(config, method.returnType)
      file.add(s"""def ${method.name}($args)($implicitArgs): Future[$s] = trace("${method.name}") { td =>""", 1)
      val argsMapped = method.args.map(arg => ThriftMethodHelper.getArgCall(arg)).mkString(", ")
      FieldTypeImports.imports(config, method.returnType).foreach(pkg => file.addImport(pkg.init, pkg.lastOption.getOrElse(throw new IllegalStateException())))
      file.add(s"val _request = Request($thriftServiceCanonicalName.${method.name.capitalize}.Args($argsMapped))")
      file.add(s"val _requestWithHeaders = injectTraceDataToHeaders(options)(headers, td).foldLeft(_request)((acc, kv) => acc.setHeader(kv._1, kv._2))")
      file.add(s"val _response = svc.${method.name}(_requestWithHeaders)")
      file.add(s"_response.map(_.value)${ThriftMethodHelper.getReturnMapping(method.returnType)}")
      file.add("}", -1)
    }
  }

  private[this] def getThriftServiceClass(svc: ExportService): Seq[String] = {
    svc.pkg.dropRight(1) :+ svc.key
  }
  private[this] def getThriftServiceClassCanonicalName(svc: ExportService): String = {
    getThriftServiceClass(svc).mkString(".")
  }
  private[this] def getThriftReqRepServicePerEndpointCanonicalName(svc: ExportService): String = {
    s"${getThriftServiceClassCanonicalName(svc)}.ReqRepServicePerEndpoint"
  }
}
