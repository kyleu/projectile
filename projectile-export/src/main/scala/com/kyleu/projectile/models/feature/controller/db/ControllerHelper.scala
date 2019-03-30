package com.kyleu.projectile.models.feature.controller.db

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.output.file.ScalaFile

object ControllerHelper {
  private[this] val relArgs = "orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None"

  def writeView(config: ExportConfiguration, file: ScalaFile, model: ExportModel, viewPkg: String) = {
    val audited = model.features(ModelFeature.Audit)
    val withNotes = model.features(ModelFeature.Notes)

    val viewArgs = model.pkFields.map(f => s"${f.propertyName}: ${f.scalaType(config)}").mkString(", ")
    val viewHtmlPackage = model.viewHtmlPackage(config).mkString(".")
    val getArgs = model.pkFields.map(_.propertyName).mkString(", ")
    val logArgs = model.pkFields.map(f => "$" + f.propertyName).mkString(", ")

    file.add(s"""def view($viewArgs, t: Option[String] = None) = withSession("view", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"""val modelF = svc.getByPrimaryKey(request, $getArgs)""")
    if (audited) {
      file.add(s"""val auditsF = auditRecordSvc.getByModel(request, "${model.propertyName}", ${model.pkFields.map(_.propertyName).mkString(", ")})""")
    }
    if (withNotes) {
      file.add(s"""val notesF = noteSvc.getFor(request, "${model.propertyName}", ${model.pkFields.map(_.propertyName).mkString(", ")})""")
    }
    file.add()

    val audMap = if (audited) { "auditsF.flatMap(audits => " } else { "" }
    val auditHelp = if (audited) { "audits, " } else { "" }

    val notesMap = if (withNotes) { "notesF.flatMap(notes => " } else { "" }
    val notesHelp = if (withNotes) { "notes, " } else { "" }

    file.add(s"""$notesMap${audMap}modelF.map {""", 1)
    file.add("case Some(model) => renderChoice(t) {", 1)
    val cfgArg = s"app.cfg(Some(request.identity), ${model.features(ModelFeature.Auth)})"
    val extraViewArgs = s"request.identity, $cfgArg, model, $notesHelp${auditHelp}app.config.debug"
    file.add(s"case MimeTypes.HTML => Ok($viewHtmlPackage.${model.propertyName}View($extraViewArgs))")
    file.add("case MimeTypes.JSON => Ok(model.asJson)")
    file.add("case ServiceController.MimeTypes.png => Ok(renderToPng(v = model)).as(ServiceController.MimeTypes.png)")
    file.add("case ServiceController.MimeTypes.svg => Ok(renderToSvg(v = model)).as(ServiceController.MimeTypes.svg)")

    file.add("}", -1)
    file.add(s"""case None => NotFound(s"No ${model.className} found with $getArgs [$logArgs]")""")
    file.add(s"}${if (withNotes) { ")" } else { "" }}${if (audited) { ")" } else { "" }}", -1)
    file.add("}", -1)
  }

  def writePks(config: ExportConfiguration, model: ExportModel, file: ScalaFile, viewPkg: String, routesClass: String) = if (model.pkFields.nonEmpty) {
    model.pkFields.foreach(_.addImport(config, file, Nil))
    val viewArgs = model.pkFields.map(f => s"${f.propertyName}: ${f.scalaType(config)}").mkString(", ")
    val callArgs = model.pkFields.map(f => s"${f.propertyName} = ${f.propertyName}").mkString(", ")
    val getArgs = model.pkFields.map(_.propertyName).mkString(", ")
    val logArgs = model.pkFields.map(f => "$" + f.propertyName).mkString(", ")
    val redirArgs = model.pkFields.map(f => "res._1." + f.propertyName).mkString(", ")
    file.add()
    writeView(config, file, model, viewPkg)
    file.add()
    file.add(s"""def editForm($viewArgs) = withSession("edit.form", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"val cancel = $routesClass.view($getArgs)")
    file.add(s"val call = $routesClass.edit($getArgs)")
    file.add(s"svc.getByPrimaryKey(request, $getArgs).map {", 1)
    file.add("case Some(model) => Ok(", 1)

    val cfgArg = s"app.cfg(Some(request.identity), ${model.features(ModelFeature.Auth)})"
    val extraArgs = "cancel, call, debug = app.config.debug"
    file.add(s"""$viewPkg.${model.propertyName}Form(request.identity, ${cfgArg}, model, s"${model.title} [$logArgs]", $extraArgs)""")
    file.add(")", -1)
    file.add(s"""case None => NotFound(s"No ${model.className} found with $getArgs [$logArgs]")""")
    file.add("}", -1)
    file.add("}", -1)
    file.add()
    file.add(s"""def edit($viewArgs) = withSession("edit", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"svc.update(request, $callArgs, fields = modelForm(request.body)).map(res => render {", 1)
    file.add(s"""case Accepts.Html() => Redirect($routesClass.view($redirArgs)).flashing("success" -> res._2)""")
    file.add("case Accepts.Json() => Ok(res.asJson)")
    file.add("})", -1)
    file.add("}", -1)
    file.add()
    file.add(s"""def remove($viewArgs) = withSession("remove", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"svc.remove(request, $callArgs).map(_ => render {", 1)
    file.add(s"case Accepts.Html() => Redirect($routesClass.list())")
    file.add("""case Accepts.Json() => Ok(io.circe.Json.obj("status" -> io.circe.Json.fromString("removed")))""")
    file.add("})", -1)
    file.add("}", -1)
  }

  def writeForeignKeys(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = model.foreignKeys.foreach { fk =>
    fk.references match {
      case h :: Nil =>
        val col = model.fields.find(_.key == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]"))
        col.addImport(config, file, Nil)
        val propId = col.propertyName
        val propCls = col.className

        file.add()
        file.add(s"""def by$propCls($propId: ${col.scalaType(config)}, $relArgs) = {""", 1)
        file.add(s"""withSession("get.by.$propId", admin = true) { implicit request => implicit td =>""", 1)
        file.add("val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq")
        file.add(s"svc.getBy$propCls(request, $propId, orderBys, limit, offset).map(models => renderChoice(t) {", 1)

        file.add(s"case MimeTypes.HTML => Ok(${model.viewHtmlPackage(config).mkString(".")}.${model.propertyName}By$propCls(", 1)
        val cfgArg = s"app.cfg(Some(request.identity), ${model.features(ModelFeature.Auth)})"
        file.add(s"""request.identity, $cfgArg, $propId, models, orderBy, orderAsc, limit.getOrElse(5), offset.getOrElse(0)""")
        file.add("))", -1)
        file.add("case MimeTypes.JSON => Ok(models.asJson)")
        file.add(s"""case ServiceController.MimeTypes.csv => csvResponse("${model.className} by $propId", svc.csvFor(0, models))""")
        file.add("case ServiceController.MimeTypes.png => Ok(renderToPng(v = models)).as(ServiceController.MimeTypes.png)")
        file.add("case ServiceController.MimeTypes.svg => Ok(renderToSvg(v = models)).as(ServiceController.MimeTypes.svg)")

        file.add("})", -1)
        file.add("}", -1)
        file.add("}", -1)
      case _ => // noop
    }
  }
}
