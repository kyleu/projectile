package com.kyleu.projectile.models.feature.controller.db

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.output.file.ScalaFile

object ControllerHelper {
  def writeView(config: ExportConfiguration, file: ScalaFile, model: ExportModel, viewPkg: String) = {
    val audited = model.features(ModelFeature.Audit)
    val withNotes = model.features(ModelFeature.Notes)

    val viewArgs = model.pkFields.map(f => s"${f.propertyName}: ${f.scalaType(config)}").mkString(", ")
    val getArgs = model.pkFields.map(_.propertyName).mkString(", ")
    val logArgs = model.pkFields.map(f => "$" + f.propertyName).mkString(", ")

    file.add(s"""def view($viewArgs, t: Option[String] = None) = withSession("view", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"""val modelF = svc.getByPrimaryKey(request, $getArgs)""")
    if (audited) {
      file.add(s"""val auditsF = auditRecordSvc.getByModel(request, "${model.className}", ${model.pkFields.map(_.propertyName).mkString(", ")})""")
    }
    if (withNotes) {
      file.add(s"""val notesF = noteSvc.getFor(request, "${model.className}", ${model.pkFields.map(_.propertyName).mkString(", ")})""")
    }
    file.add()

    val audMap = if (audited) { "auditsF.flatMap(audits => " } else { "" }
    val notesMap = if (withNotes) { "notesF.flatMap(notes => " } else { "" }
    file.add(s"""$notesMap${audMap}modelF.map {""", 1)
    file.add("case Some(model) => renderChoice(t) {", 1)

    val viewArg = getViewArg(config, model, audited, withNotes)

    file.add(s"case MimeTypes.HTML => $viewArg")
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

    val cfgArg = s"""app.cfgAdmin(request.identity, "${model.firstPackage}", "${model.key}", "Edit")"""
    val extraArgs = "cancel, call, debug = app.config.debug"
    file.add(s"""$viewPkg.${model.propertyName}Form($cfgArg, model, s"${model.title} [$logArgs]", $extraArgs)""")
    file.add(")", -1)
    file.add(s"""case None => NotFound(s"No ${model.className} found with $getArgs [$logArgs]")""")
    file.add("}", -1)
    file.add("}", -1)
    file.add()
    file.add(s"""def edit($viewArgs) = withSession("edit", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"svc.update(request, $callArgs, fields = modelForm(request.body)).map(res => render {", 1)
    file.add(s"""case Accepts.Html() => Redirect($routesClass.view($redirArgs))""")
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

  private[this] def getViewArg(config: ExportConfiguration, model: ExportModel, audited: Boolean, withNotes: Boolean) = {
    val viewHtmlPackage = model.viewHtmlPackage(config).mkString(".")
    val auditHelp = if (audited) { "audits, " } else { "" }
    val notesHelp = if (withNotes) { "notes, " } else { "" }
    val keyString = model.pkFields match {
      case head :: Nil if head.t == FieldType.StringType => ", model." + head.propertyName
      case head :: Nil => ", model." + head.propertyName + ".toString"
      case Nil => ", \"Detail\""
      case _ => ", s\"" + model.pkFields.map(f => "${model." + f.propertyName + "}").mkString(", ") + "\""
    }
    val cfgArg = s"""app.cfgAdmin(u = request.identity, "${model.firstPackage}", "${model.key}"$keyString)"""
    val extraViewArgs = s"$cfgArg, model, $notesHelp${auditHelp}app.config.debug"
    s"Ok($viewHtmlPackage.${model.propertyName}View($extraViewArgs))"
  }
}
