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

    config.addCommonImport(file, "Credentials")

    file.add(s"""def view($viewArgs, t: Option[String] = None) = withSession("view", ${model.perm("view")}) { implicit request => implicit td =>""", 1)
    file.add("""val creds: Credentials = request""")
    file.add(s"""val modelF = svc.getByPrimaryKeyRequired(creds, $getArgs)""")
    if (audited) {
      file.add(s"""val auditsF = auditSvc.getByModel(creds, "${model.className}", ${model.pkFields.map(_.propertyName).mkString(", ")})""")
    }
    if (withNotes) {
      file.add(s"""val notesF = noteSvc.getFor(creds, "${model.className}", ${model.pkFields.map(_.propertyName).mkString(", ")})""")
    }
    val fkConnections = getFkConnections(config, model)
    fkConnections.foreach {
      case (srcProp, tgtModel) if srcProp.required =>
        file.add(s"val ${srcProp.propertyName}F = modelF.flatMap(m => ${tgtModel.propertyName}S.getByPrimaryKey(creds, m.${srcProp.propertyName}))")
      case (srcProp, tgtModel) =>
        file.add(s"val ${srcProp.propertyName}F = modelF.flatMap(m => m.${srcProp.propertyName}.map(v => ${tgtModel.propertyName}S.getByPrimaryKey(creds, v)).getOrElse(Future.successful(None)))")
    }
    file.add()
    if (fkConnections.nonEmpty) {
      file.add(fkConnections.map(x => s"${x._1.propertyName}F.flatMap(${x._1.propertyName}R => ").mkString.trim, 1)
    }
    val audMap = if (audited) { "auditsF.flatMap(audits => " } else { "" }
    val notesMap = if (withNotes) { "notesF.flatMap(notes => " } else { "" }
    file.add(s"""$notesMap${audMap}modelF.map { model =>""", 1)
    file.add("renderChoice(t) {", 1)

    val viewExtra = fkConnections.map(x => s"${x._1.propertyName}R, ").mkString
    val viewArg = getViewArg(config, model, audited, withNotes, Some(viewExtra))

    file.add(s"case MimeTypes.HTML => $viewArg")
    file.add("case MimeTypes.JSON => Ok(model.asJson)")

    file.add("}", -1)
    file.add(s"}${if (withNotes) { ")" } else { "" }}${if (audited) { ")" } else { "" }}${fkConnections.map(_ => ")").mkString}", -1)
    file.add("}", if (fkConnections.isEmpty) { -1 } else { -2 })
  }

  def getFkConnections(config: ExportConfiguration, model: ExportModel) = {
    model.foreignKeys.filter(_.references.size == 1).map { fk =>
      val ref = fk.references.headOption.getOrElse(throw new IllegalStateException())
      (model.getField(ref.source), config.getModel(fk.targetTable, "fk lookup"))
    }
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
    file.add(s"""def editForm($viewArgs) = withSession("edit.form", ${model.perm("edit")}) { implicit request => implicit td =>""", 1)
    file.add(s"val cancel = $routesClass.view($getArgs)")
    file.add(s"val call = $routesClass.edit($getArgs)")
    file.add(s"svc.getByPrimaryKey(request, $getArgs).map {", 1)
    file.add("case Some(model) => Ok(", 1)

    val cfgArg = s"""app.cfg(Some(request.identity), "${model.firstPackage}", "${model.key}", "Edit")"""
    val extraArgs = "cancel, call, debug = app.config.debug"
    file.add(s"""$viewPkg.${model.propertyName}Form($cfgArg, model, s"${model.title} [$logArgs]", $extraArgs)""")
    file.add(")", -1)
    file.add(s"""case None => NotFound(s"No ${model.className} found with $getArgs [$logArgs]")""")
    file.add("}", -1)
    file.add("}", -1)
    file.add()
    file.add(s"""def edit($viewArgs) = withSession("edit", ${model.perm("edit")}) { implicit request => implicit td =>""", 1)
    file.add(s"svc.update(request, $callArgs, fields = modelForm(request.body)).map(res => render {", 1)
    file.add(s"""case Accepts.Html() => Redirect($routesClass.view($redirArgs))""")
    file.add("case Accepts.Json() => Ok(res.asJson)")
    file.add("})", -1)
    file.add("}", -1)
    file.add()
    file.add(s"""def remove($viewArgs) = withSession("remove", ${model.perm("edit")}) { implicit request => implicit td =>""", 1)
    file.add(s"svc.remove(request, $callArgs).map(_ => render {", 1)
    file.add(s"case Accepts.Html() => Redirect($routesClass.list())")
    file.add("""case Accepts.Json() => Ok(io.circe.Json.obj("status" -> io.circe.Json.fromString("removed")))""")
    file.add("})", -1)
    file.add("}", -1)
  }

  private[this] def getViewArg(config: ExportConfiguration, model: ExportModel, audited: Boolean, withNotes: Boolean, viewExtra: Option[String]) = {
    val viewHtmlPackage = model.viewHtmlPackage(config).mkString(".")
    val auditHelp = if (audited) { "audits, " } else { "" }
    val notesHelp = if (withNotes) { "notes, " } else { "" }
    val keyString = model.pkFields match {
      case head :: Nil if head.t == FieldType.StringType => ", model." + head.propertyName
      case head :: Nil => ", model." + head.propertyName + ".toString"
      case Nil => ", \"Detail\""
      case _ => ", s\"" + model.pkFields.map(f => "${model." + f.propertyName + "}").mkString(", ") + "\""
    }
    val cfgArg = s"""app.cfg(u = Some(request.identity), "${model.firstPackage}", "${model.key}"$keyString)"""
    val extraViewArgs = s"$cfgArg, model, $notesHelp$auditHelp${viewExtra.getOrElse("")}app.config.debug"
    s"Ok($viewHtmlPackage.${model.propertyName}View($extraViewArgs))"
  }
}
