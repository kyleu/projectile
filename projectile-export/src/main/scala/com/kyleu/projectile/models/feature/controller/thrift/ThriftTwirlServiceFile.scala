package com.kyleu.projectile.models.feature.controller.thrift

import com.kyleu.projectile.models.export.{ExportMethod, ExportService}
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.CommonImportHelper
import com.kyleu.projectile.models.output.file.TwirlFile

object ThriftTwirlServiceFile {
  def export(config: ExportConfiguration, service: ExportService) = {
    val file = TwirlFile(config.viewPackage ++ Seq("admin", "thrift"), service.propertyName)

    val cfg = CommonImportHelper.getString(config, "UiConfig")

    file.add(s"@(cfg: $cfg, debug: Boolean = false)(")
    file.add("    implicit request: Request[AnyContent], session: Session, flash: Flash")
    val systemViewPkg = (config.systemViewPackage :+ "html").mkString(".")
    file.add(s""")@$systemViewPkg.layout.page(title = "${service.className}", cfg = cfg) {""", 1)
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
