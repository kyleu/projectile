package com.kyleu.projectile.models.feature.controller.thrift

import com.kyleu.projectile.models.export.{ExportMethod, ExportService}
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.CommonImportHelper
import com.kyleu.projectile.models.output.file.TwirlFile

object ThriftTwirlServiceFile {
  def export(config: ExportConfiguration, service: ExportService) = {
    val file = TwirlFile(config.viewPackage ++ Seq("admin", "thrift"), service.propertyName)

    val aa = CommonImportHelper.getString(config, "AuthActions")
    val td = CommonImportHelper.getString(config, "TraceData")
    val su = CommonImportHelper.getString(config, "SystemUser")

    file.add(s"@(user: $su, authActions: $aa, debug: Boolean = false)(")
    file.add(s"    implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: $td")
    file.add(s""")@${config.systemViewPackage.mkString(".")}.html.admin.layout.page(user, authActions, "thrift", "${service.className}") {""", 1)
    file.add("""<div class="row">""", 1)
    file.add("""<div class="col s12">""", 1)
    file.add("""<div class="collection with-header">""", 1)

    file.add("""<div class="collection-header">""", 1)
    file.add(s"<h4>${service.className}</h4>")
    file.add(s"<em>${service.pkg.mkString(".")}</em>")
    file.add("</div>", -1)
    file.add(s"""<div class="collection-item">@${config.viewPackage.mkString(".")}.html.admin.thrift.thriftServiceRef("${service.className}")</div>""")

    val routesRef = s"${service.pkg.mkString(".")}.controllers.${service.propertyName}.routes.${service.className}Controller"
    service.methods.foreach(m => methodLink(file, m, routesRef))

    file.add("</div>", -1)
    file.add("</div>", -1)
    file.add("</div>", -1)
    file.add("}", -1)
    file
  }

  private[this] def methodLink(file: TwirlFile, m: ExportMethod, ref: String) = {
    file.add(s"""<a class="theme-text collection-item" href="@$ref.${m.name}">${m.signature}</a>""")
  }
}
