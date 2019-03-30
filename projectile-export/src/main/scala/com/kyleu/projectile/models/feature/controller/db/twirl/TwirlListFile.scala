package com.kyleu.projectile.models.feature.controller.db.twirl

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.CommonImportHelper
import com.kyleu.projectile.models.output.file.TwirlFile

object TwirlListFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val listFile = TwirlFile(model.viewPackage(config), model.propertyName + "List")
    val viewArgs = "q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Int, offset: Int"
    val modelPkg = (config.applicationPackage :+ "models").mkString(".")
    val listCalls = (config.systemPackage ++ Seq("models", "result", "web", "ListCalls")).mkString(".")

    val su = CommonImportHelper.getString(config, "SystemUser")

    val finalArgs = s"user: $su, cfg: ${CommonImportHelper.getString(config, "UiConfig")}"
    listFile.add(s"@($finalArgs, totalCount: Option[Int], modelSeq: Seq[${model.fullClassPath(config)}], $viewArgs)(", 2)
    if (config.isNewUi) {
      listFile.add(s"implicit request: Request[AnyContent], session: Session, flash: Flash")
    } else {
      val td = config.utilitiesPackage.mkString(".") + ".tracing.TraceData"
      listFile.add(s"implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: $td")
    }
    listFile.add(")", -2)

    listFile.add(s"@${(config.systemViewPackage :+ "html").mkString(".")}.admin.explore.list(", 1)
    listFile.add("user = user,")
    listFile.add("cfg = cfg,")
    listFile.add(s"""model = "${model.title}",""")
    listFile.add(s"""modelPlural = "${model.plural}",""")
    listFile.add(s"icon = $modelPkg.template.Icons.${model.propertyName},")
    listFile.add("cols = Seq(", 1)
    model.searchFields.foreach {
      case c if model.searchFields.lastOption.contains(c) => listFile.add(s""""${c.propertyName}" -> "${c.title}"""")
      case c => listFile.add(s""""${c.propertyName}" -> "${c.title}",""")
    }
    listFile.add("),", -1)
    listFile.add("totalCount = totalCount,")
    val viewPkg = model.viewHtmlPackage(config).mkString(".")
    listFile.add(s"rows = modelSeq.map(model => $viewPkg.${model.propertyName}DataRow(model)),")
    listFile.add("orderBy = orderBy,")
    listFile.add("orderAsc = orderAsc,")
    listFile.add(s"calls = $listCalls(", 1)
    val rc = TwirlHelper.routesClass(config, model)
    listFile.add(s"newCall = Some($rc.createForm()),")
    listFile.add(s"orderBy = Some($rc.list(q, _, _, Some(limit), Some(0))),")
    listFile.add(s"search = Some($rc.list(None, orderBy, orderAsc, Some(limit), None)),")
    listFile.add(s"next = $rc.list(q, orderBy, orderAsc, Some(limit), Some(offset + limit)),")
    listFile.add(s"prev = $rc.list(q, orderBy, orderAsc, Some(limit), Some(offset - limit))")
    listFile.add("),", -1)
    listFile.add("limit = limit,")
    listFile.add("offset = offset,")
    listFile.add("q = q")
    listFile.add(")", -1)

    listFile
  }
}
