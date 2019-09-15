// scalastyle:off file.size.limit
package com.kyleu.projectile.models.feature.controller.db

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.project.ProjectFlag

object ControllerMutations {
  def addBulkEdit(config: ExportConfiguration, file: ScalaFile, model: ExportModel) = {
    if (model.foreignKeys.nonEmpty && (!config.project.flags(ProjectFlag.NoBulk))) {
      file.add(s"""def bulkEdit = withSession("bulk.edit", ${model.perm("edit")}) { implicit request => implicit td =>""", 1)
      config.addCommonImport(file, "ControllerUtils")
      file.add("""val form = ControllerUtils.getForm(request.body)""")
      val split = """_.split("---").map(_.trim).filter(_.nonEmpty).toList"""
      file.add(s"""val pks = form("primaryKeys").split("//").map(_.trim).filter(_.nonEmpty).map($split).toList""")
      file.add("""val changes = modelForm(request.body)""")
      file.add("""svc.updateBulk(request, pks, changes).map(msg => Ok("OK: " + msg))""")
      file.add("}", -1)
    }
  }

  def addMutations(config: ExportConfiguration, file: ScalaFile, model: ExportModel, routesClass: String, viewHtmlPackage: String) = {
    if (!model.readOnly) {
      file.add(s"""def createForm = withSession("create.form", ${model.perm("edit")}) { implicit request => implicit td =>""", 1)
      file.add(s"val cancel = $routesClass.list()")
      file.add(s"val call = $routesClass.create()")
      file.add(s"Future.successful(Ok($viewHtmlPackage.${model.propertyName}Form(", 1)
      val cfgArg = s"""app.cfg(u = Some(request.identity), "${model.firstPackage}", "${model.key}", "Create")"""
      file.add(s"""$cfgArg, ${model.className}.empty(), "New ${model.title}", cancel, call, isNew = true, debug = app.config.debug""")
      file.add(")))", -1)
      file.add("}", -1)
      file.add()
      file.add(s"""def create = withSession("create", ${model.perm("edit")}) { implicit request => implicit td =>""", 1)
      file.add("svc.create(request, modelForm(request.body)).map {", 1)
      if (model.pkFields.isEmpty) {
        file.add("case Some(_) => throw new IllegalStateException(\"No primary key.\")")
      } else {
        val viewArgs = model.pkFields.map("model." + _.propertyName).mkString(", ")
        file.add(s"case Some(model) => Redirect($routesClass.view($viewArgs))")
      }
      file.add(s"case None => Redirect($routesClass.list())")
      file.add("}", -1)
      file.add("}", -1)
      file.add()
    }
  }

  def addImports(config: ExportConfiguration, file: ScalaFile, model: ExportModel) = {
    file.addImport(model.modelPackage(config), model.className)
    config.addCommonImport(file, "Application")
    config.addCommonImport(file, "BaseController")
    if (model.features(ModelFeature.Auth)) {
      config.addCommonImport(file, "ServiceAuthController")
    } else {
      config.addCommonImport(file, "ServiceController")
    }
    if (model.features(ModelFeature.Audit)) { config.addCommonImport(file, "AuditService") }
    config.addCommonImport(file, "OrderBy")
    config.addCommonImport(file, "JsonSerializers", "_")
    config.addCommonImport(file, "DateUtils")
    config.addCommonImport(file, "ExecutionContext")
    file.addImport(Seq("scala", "concurrent"), "Future")
    file.addImport(Seq("play", "api", "http"), "MimeTypes")
    file.addImport(model.servicePackage(config), model.className + "Service")
    file.addImport(model.modelPackage(config), model.className + "Result")
  }
}
