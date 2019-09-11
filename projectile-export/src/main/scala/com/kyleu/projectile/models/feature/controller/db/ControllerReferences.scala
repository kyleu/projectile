package com.kyleu.projectile.models.feature.controller.db

import com.kyleu.projectile.models.export.{ExportModel, ExportModelReference}
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.file.ScalaFile

object ControllerReferences {
  val relArgs = "orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None, embedded: Boolean = false"

  def write(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val references = ExportModelReference.transformedReferences(config, model)
    if (references.nonEmpty) {
      config.addCommonImport(file, "RelationCount")

      val pkRefs = model.pkFields.map(_.propertyName).mkString(", ")
      val pkArgs = model.pkFields.map(f => s"${f.propertyName}: ${f.scalaType(config)}").mkString(", ")

      file.add(s"""def relationCounts($pkArgs) = withSession("relation.counts", ${model.perm("view")}) { implicit request => implicit td =>""", 1)

      references.foreach { r =>
        file.add(s"val ${r.src.propertyName}By${r.tf.className}F = ${r.src.propertyName}S.countBy${r.tf.className}(request, $pkRefs)")
      }
      val forArgs = references.map(r => s"${r.src.propertyName}By${r.tf.className}C <- ${r.src.propertyName}By${r.tf.className}F").mkString("; ")
      file.add(s"for ($forArgs) yield {", 1)

      file.add("Ok(Seq(", 1)
      references.foreach { r =>
        val count = s"${r.src.propertyName}By${r.tf.className}C"
        val comma = if (references.lastOption.contains(r)) { "" } else { "," }
        file.add(s"""RelationCount(model = "${r.src.propertyName}", field = "${r.tf.propertyName}", count = $count)$comma""")
      }
      file.add(").asJson)", -1)
      file.add("}", -1)
      file.add("}", -1)
    }
  }

  def refServiceArgs(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val refServices = ExportModelReference.validReferences(config, model).map(_.srcTable).distinct.map(m => config.getModel(m, "reference args"))
    refServices.foreach(s => file.addImport(s.servicePackage(config), s.className + "Service"))
    refServices.map(s => s.propertyName + "S: " + s.className + "Service").mkString(", ")
  }

  def writeForeignKeys(config: ExportConfiguration, model: ExportModel, file: ScalaFile, viewHtmlPackage: String) = model.foreignKeys.foreach { fk =>
    fk.references match {
      case h :: Nil =>
        val col = model.getField(h.source)
        col.addImport(config, file, Nil)
        val propId = col.propertyName
        val propCls = col.className

        file.add()
        file.add(s"""def by$propCls($propId: ${col.scalaType(config)}, $relArgs) = {""", 1)
        file.add(s"""withSession("get.by.$propId", ${model.perm("view")}) { implicit request => implicit td =>""", 1)
        file.add("val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq")
        file.add(s"svc.getBy$propCls(request, $propId, orderBys, limit, offset).map(models => renderChoice(t) {", 1)

        def cfgArg(s: String) = s"""app.cfg(Some(request.identity), "${model.firstPackage}", "${model.key}", "$s")"""
        val args = s"""$propId, models, orderBy, orderAsc, limit.getOrElse(5), offset.getOrElse(0)"""
        val call = s"${model.viewHtmlPackage(config).mkString(".")}.${model.propertyName}By$propCls"

        file.addImport(config.systemPackage ++ Seq("views", "html", "layout"), "page")
        file.addImport(config.systemPackage ++ Seq("views", "html", "layout"), "card")
        file.add("case MimeTypes.HTML =>", 1)
        file.add(s"val cfg = ${cfgArg(col.title)}")
        file.add(s"val list = $call(cfg, $args)")
        val fullCall = s"""Ok(page(s"${model.plural} by ${col.title} [$$$propId]", cfg)(card(None)(list)))"""
        file.add(s"""if (embedded) { Ok(list) } else { $fullCall }""")
        file.indent(-1)

        file.add("case MimeTypes.JSON => Ok(models.asJson)")
        file.add(s"""case BaseController.MimeTypes.csv => csvResponse("${model.className} by $propId", svc.csvFor(0, models))""")

        file.add("})", -1)
        file.add("}", -1)
        file.add("}", -1)
        file.add()
        file.add(s"""def by${propCls}BulkForm($propId: ${col.scalaType(config)}) = {""", 1)
        file.add(s"""withSession("get.by.$propId", ${model.perm("edit")}) { implicit request => implicit td =>""", 1)
        file.add(s"svc.getBy$propCls(request, $propId).map { modelSeq =>", 1)
        file.add(s"val act = ${model.routesPackage(config).mkString(".")}.${model.className}Controller.bulkEdit()")
        file.add(s"Ok($viewHtmlPackage.${model.propertyName}BulkForm(${cfgArg("Bulk Edit")}, modelSeq, act, debug = app.config.debug))")
        file.add("}", -1)
        file.add("}", -1)
        file.add("}", -1)
      case _ => // noop
    }
  }
}
