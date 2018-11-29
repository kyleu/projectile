package com.projectile.models.feature.core.thrift

import com.projectile.models.export.ExportService
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.OutputPath
import com.projectile.models.output.file.ScalaFile

object ThriftServiceFile {
  def export(config: ExportConfiguration, svc: ExportService) = {
    val file = ScalaFile(path = OutputPath.ServerSource, dir = svc.pkg :+ "services", key = svc.className)

    file.addImport(Seq("scala", "concurrent"), "Future")
    file.addImport(svc.pkg :+ svc.key, "MethodPerEndpoint")

    config.addCommonImport(file, "TraceData")
    config.addCommonImport(file, "ThriftFutureUtils", "toScalaFuture")
    config.addCommonImport(file, "ThriftService")
    config.addCommonImport(file, "ThriftServiceHelper")

    file.add(s"object ${svc.className} extends ThriftService(", 1)
    file.add(s"""key = "${svc.key}",""")
    file.add(s"""pkg = "${svc.pkg.mkString(".")}",""")
    file.add(s"""route = "/admin/thrift/${svc.propertyName.stripSuffix("Service")}"""")
    file.add(")", -1)
    file.add()
    file.add("@javax.inject.Singleton")
    file.add(s"""class ${svc.className} @javax.inject.Inject() (svc: MethodPerEndpoint) extends ThriftServiceHelper("${svc.key}") {""", 1)
    addMethods(config, file, svc)
    file.add("}", -1)

    file
  }

  private[this] def addMethods(config: ExportConfiguration, file: ScalaFile, svc: ExportService) = {
    svc.methods.foreach { method =>
      file.add(s"// $method")
      /*
      val args = method.arguments.map { a =>
        val colType = ThriftFileHelper.columnTypeFor(a.t, metadata)._1
        ThriftFileHelper.declarationFor(required = a.required || a.value.isDefined, name = a.name, value = a.value, metadata = metadata, colType = colType)
      }.mkString(", ")
      val retType = ThriftFileHelper.columnTypeFor(method.returnValue, metadata)._1
      file.add()
      file.add(s"""def ${method.name}($args)(implicit td: TraceData): Future[$retType] = trace("${method.name}") { _ =>""", 1)
      val argsMapped = method.arguments.map(arg => ThriftMethodHelper.getArgCall(arg, metadata)).mkString(", ")
      file.add(s"svc.${method.name}($argsMapped)${ThriftMethodHelper.getReturnMapping(retType)}")
      file.add("}", -1)
      */
    }
  }
}
