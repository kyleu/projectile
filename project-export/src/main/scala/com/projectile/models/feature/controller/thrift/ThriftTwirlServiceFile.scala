package com.projectile.models.feature.controller.thrift

import com.projectile.models.export.{ExportMethod, ExportService}
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.CommonImportHelper
import com.projectile.models.output.file.TwirlFile

object ThriftTwirlServiceFile {
  def export(config: ExportConfiguration, service: ExportService) = {
    val file = TwirlFile(config.viewPackage ++ Seq("admin", "thrift"), service.propertyName)

    val td = CommonImportHelper.get(config, file, "TraceData")
    val su = CommonImportHelper.get(config, file, "SystemUser")

    file.add(s"@(user: ${(su._1 :+ su._2).mkString(".")}, debug: Boolean = false)(")
    file.add(s"    implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: ${(td._1 :+ td._2).mkString(".")}")
    val trace = "@traceData.logClass(getClass)"
    file.add(s""")$trace@${config.viewPackage.mkString(".")}.html.admin.layout.page(user, "projects", "${service.className}") {""", 1)
    file.add("""<div class="row">""", 1)
    file.add("""<div class="col s12">""", 1)
    file.add("""<div class="collection with-header">""", 1)

    file.add("""<div class="collection-header">""", 1)
    file.add(s"<h4>${service.className}</h4>")
    file.add(s"<em>${service.pkg.mkString(".")}</em>")
    file.add("</div>", -1)

    val routesRef = s"${service.pkg.mkString(".")}.controllers.${service.propertyName}.routes.${service.className}Controller"
    service.methods.foreach(m => methodLink(file, m, routesRef))

    file.add("</div>", -1)
    file.add("</div>", -1)
    file.add("</div>", -1)
    file.add("}", -1)
    file
  }

  private[this] def methodLink(file: TwirlFile, m: ExportMethod, ref: String) = {
    file.add(s"""<a class="theme-text collection-item" href="@$ref.${m.key}">${m.signature}</a>""")
  }
}
