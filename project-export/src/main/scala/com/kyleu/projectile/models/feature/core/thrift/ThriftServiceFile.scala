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

    file.addImport(thriftService, "MethodPerEndpoint")

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
      val args = method.args.map(a => ThriftFileHelper.declarationForField(config, a)).mkString(", ")
      method.args.foreach(a => a.addImport(config, file, svc.pkg))
      file.add()
      val s = FieldTypeAsScala.asScala(config, method.returnType)
      file.add(s"""def ${method.key}($args)(implicit td: TraceData): Future[$s] = trace("${method.key}") { _ =>""", 1)
      val argsMapped = method.args.map(arg => ThriftMethodHelper.getArgCall(arg)).mkString(", ")

      FieldTypeImports.imports(config, method.returnType).foreach(pkg => file.addImport(pkg.init, pkg.last))

      file.add(s"svc.${method.key}($argsMapped)${ThriftMethodHelper.getReturnMapping(method.returnType)}")
      file.add("}", -1)
    }
  }
}
