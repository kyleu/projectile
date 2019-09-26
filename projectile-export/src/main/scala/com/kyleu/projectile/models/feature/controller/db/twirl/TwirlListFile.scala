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
    val listCalls = (config.systemPackage ++ Seq("models", "web", "ListCalls")).mkString(".")

    val finalArgs = s"cfg: ${CommonImportHelper.getString(config, "UiConfig")}"
    listFile.add(s"@($finalArgs, totalCount: Option[Int], modelSeq: Seq[${model.fullClassPath(config)}], $viewArgs)(", 2)
    val tdi = CommonImportHelper.get(config, "TraceData")._1.mkString(".")
    listFile.add(s"implicit request: Request[AnyContent], flash: Flash, td: $tdi.TraceData")
    listFile.add(")", -2)

    val imp = CommonImportHelper.get(config, "AugmentService")._1.mkString(".")
    listFile.add(s"@defining($imp.AugmentService.lists.augment(models = modelSeq, args = request.queryString, cfg = cfg)) { aug =>", 1)
    listFile.add(s"@${(config.systemViewPackage :+ "html").mkString(".")}.admin.explore.list(", 1)
    listFile.add("cfg = cfg,")
    listFile.add(s"""model = "${model.title}",""")
    listFile.add(s"""modelPlural = "${model.plural}",""")
    if (model.provided) {
      listFile.add(s"icon = $modelPkg.web.InternalIcons.${model.propertyName},")
    } else {
      listFile.add(s"icon = $modelPkg.template.Icons.${model.propertyName},")
    }
    listFile.add("cols = Seq(", 1)
    (model.pkFields ++ model.summaryFields).distinct.foreach {
      case c if model.summaryFields.lastOption.contains(c) => listFile.add(s""""${c.propertyName}" -> "${c.title}"""")
      case c => listFile.add(s""""${c.propertyName}" -> "${c.title}",""")
    }
    listFile.add("),", -1)
    listFile.add("totalCount = totalCount,")
    val viewPkg = model.viewHtmlPackage(config).mkString(".")
    listFile.add(s"rows = modelSeq.map(model => $viewPkg.${model.propertyName}DataRow(model, additional = aug._2.get(model).flatten)),")
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
    listFile.add("q = q,")
    val augArgs = s"cls = classOf[${model.fullClassPath(config)}], args = request.queryString, cfg = cfg"
    listFile.add(s"additionalHeader = $imp.AugmentService.listHeaders.augment($augArgs),")
    if (model.pkFields.nonEmpty) {
      val act = model.routesPackage(config).mkString(".") + "." + model.className + "Controller.bulkEditForm()"
      listFile.add(s"""additionalAction = Some(Html(s"<a href='$${$act}' class='btn-flat'>Bulk Edit</a>")),""")
    }
    listFile.add("additionalColumns = aug._1")
    listFile.add(")", -1)
    listFile.add("}", -1)

    listFile
  }
}
