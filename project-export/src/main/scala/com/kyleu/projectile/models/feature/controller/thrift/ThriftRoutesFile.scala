package com.kyleu.projectile.models.feature.controller.thrift

import com.kyleu.projectile.models.export.{ExportMethod, ExportService}
import com.kyleu.projectile.models.output.file.RoutesFile

object ThriftRoutesFile {
  private[this] def ws(s: String, i: Int = 60) = s + (0 until (i - s.length)).map(_ => ' ').mkString

  def export(service: ExportService) = {
    val file = RoutesFile("thrift" + service.className)
    val controllerRef = s"${service.pkg.mkString(".")}.com.kyleu.projectile.web.controllers.${service.propertyName}.${service.className}Controller"

    file.add(s"# ${service.className} Routes")
    file.add(s"GET  /${ws("")} $controllerRef.list()")
    file.add()
    service.methods.foreach(m => routeForMethod(m, controllerRef, file))
    file
  }

  def routeForMethod(m: ExportMethod, controllerRef: String, file: RoutesFile) = {
    file.add(s"GET  /${ws(m.name)} $controllerRef.${m.name}")
    file.add(s"POST /${ws(m.name)} $controllerRef.${m.name}Call")
    file.add()
  }
}
