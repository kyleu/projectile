package models.output.feature.controller.twirl

import models.export.ExportModel
import models.export.config.ExportConfiguration
import models.output.file.TwirlFile

object TwirlListFile {

  def export(config: ExportConfiguration, model: ExportModel) = {
    val listFile = TwirlFile(config.applicationPackage ++ model.viewPackage, model.propertyName + "List")
    val viewArgs = "q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Int, offset: Int"
    val modelPkg = (config.applicationPackage :+ "models").mkString(".")

    listFile.add(s"@(user: $modelPkg.user.SystemUser, totalCount: Option[Int], modelSeq: Seq[${model.fullClassPath(config)}], $viewArgs)(", 2)
    val td = config.utilitiesPackage.mkString(".") + ".tracing.TraceData"
    listFile.add(s"implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: $td")
    listFile.add(s")@traceData.logViewClass(getClass)", -2)
    listFile.add(s"@${(config.applicationPackage :+ "views" :+ "html").mkString(".")}.admin.explore.list(", 1)
    listFile.add("user = user,")
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
    val viewPkg = (config.applicationPackage ++ model.viewHtmlPackage).mkString(".")
    listFile.add(s"rows = modelSeq.map(model => $viewPkg.${model.propertyName}DataRow(model)),")
    listFile.add("orderBy = orderBy,")
    listFile.add("orderAsc = orderAsc,")
    listFile.add(s"calls = $modelPkg.result.web.ListCalls(", 1)
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
