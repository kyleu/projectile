package com.kyleu.projectile.models.feature.controller.db

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile

object ControllerFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ScalaFile(path = OutputPath.ServerSource, model.controllerPackage(config), model.className + "Controller")
    val viewHtmlPackage = model.viewHtmlPackage(config).mkString(".")
    val routesClass = (model.routesPackage(config) :+ (model.className + "Controller")).mkString(".")

    file.addImport(model.modelPackage(config), model.className)
    config.addCommonImport(file, "Application")

    config.addCommonImport(file, "ServiceController")
    if (model.features(ModelFeature.Auth)) { config.addCommonImport(file, "ServiceAuthController") }
    if (model.features(ModelFeature.Audit)) { config.addCommonImport(file, "AuditService") }

    config.addCommonImport(file, "OrderBy")

    config.addCommonImport(file, "JsonSerializers", "_")
    config.addCommonImport(file, "DateUtils")
    config.addCommonImport(file, "ExecutionContext")
    config.addCommonImport(file, "ReftreeUtils", "_")

    file.addImport(Seq("scala", "concurrent"), "Future")
    file.addImport(Seq("play", "api", "http"), "MimeTypes")

    file.addImport(model.servicePackage(config), model.className + "Service")
    file.addImport(model.modelPackage(config), model.className + "Result")

    if (model.propertyName != "audit") {
      if (model.pkFields.nonEmpty) { file.addMarkers("string-search", model.key) }
      model.pkFields match {
        case sole :: Nil => sole.t match {
          case FieldType.UuidType => file.addMarkers("uuid-search", model.key)
          case FieldType.IntegerType => file.addMarkers("int-search", model.key)
          case _ => // noop
        }
        case _ => // noop
      }
    }

    file.add("@javax.inject.Singleton")
    file.add(s"class ${model.className}Controller @javax.inject.Inject() (", 2)

    if (model.features(ModelFeature.Notes)) { config.addCommonImport(file, "NoteService") }

    val extraSvcs = {
      val n = if (model.features(ModelFeature.Notes)) { ", noteSvc: NoteService" } else { "" }
      val a = if (model.features(ModelFeature.Audit)) { ", auditRecordSvc: AuditService" } else { "" }
      n + a
    }

    ControllerReferences.refServiceArgs(config, model, file) match {
      case ref if ref.trim.isEmpty => file.add(s"override val app: Application, svc: ${model.className}Service$extraSvcs")
      case ref =>
        file.add(s"override val app: Application, svc: ${model.className}Service$extraSvcs,")
        file.add(ref)
    }
    val controller = if (model.features(ModelFeature.Auth)) { "ServiceAuthController" } else { "ServiceController" }
    file.add(s")(implicit ec: ExecutionContext) extends $controller(svc) {", -2)
    file.indent()

    config.addCommonImport(file, "PermissionService")
    val firstPkg = model.pkg.headOption.getOrElse("system")
    val ico = s"""Some(${(config.applicationPackage :+ "models").mkString(".")}.template.Icons.${model.propertyName})"""
    file.add(s"""PermissionService.registerModel("$firstPkg", "${model.className}", "${model.title}", $ico, "view", "edit")""")

    file.add()
    addMutations(config, file, model, routesClass, viewHtmlPackage)
    addListAction(config, file, model, viewHtmlPackage)
    file.add("""def autocomplete(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int]) = {""", 1)
    file.add(s"""withSession("autocomplete", ${model.perm("view")}) { implicit request => implicit td =>""", 1)
    file.add("val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq")
    file.add("search(q, orderBys, limit, None).map(r => Ok(r.map(_.toSummary).asJson))")
    file.add("}", -1)
    file.add("}", -1)
    ControllerReferences.writeForeignKeys(config, model, file)
    ControllerHelper.writePks(config, model, file, viewHtmlPackage, routesClass)
    ControllerReferences.write(config, model, file)
    file.add("}", -1)
    file
  }

  private[this] def addListAction(config: ExportConfiguration, file: ScalaFile, model: ExportModel, viewHtmlPackage: String) = {
    val routesClass = (model.routesPackage(config) :+ (model.className + "Controller")).mkString(".")
    val listArgs = "orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None"
    file.add(s"""def list(q: Option[String], $listArgs) = {""", 1)
    file.add(s"""withSession("view", ${model.perm("view")}) { implicit request => implicit td =>""", 1)
    file.add("val startMs = DateUtils.nowMillis")
    file.add("val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq")
    file.add("searchWithCount(q, orderBys, limit, offset).map(r => renderChoice(t) {", 1)

    file.add(s"case MimeTypes.HTML => r._2.toList match {", 1)

    val redirArgs = model.pkFields.map(f => "model." + f.propertyName).mkString(", ")
    file.add(s"case model :: Nil if q.nonEmpty => Redirect($routesClass.view($redirArgs))")

    val cfgArg = s"""app.cfg(u = Some(request.identity), "${model.firstPackage}", "${model.key}")"""
    val args = s"$cfgArg, Some(r._1), r._2, q, orderBy, orderAsc, limit.getOrElse(100), offset.getOrElse(0)"
    file.add(s"case _ => Ok($viewHtmlPackage.${model.propertyName}List($args))")

    file.add("}", -1)

    file.add(s"case MimeTypes.JSON => Ok(${model.className}Result.fromRecords(q, Nil, orderBys, limit, offset, startMs, r._1, r._2).asJson)")
    file.add(s"""case ServiceController.MimeTypes.csv => csvResponse("${model.className}", svc.csvFor(r._1, r._2))""")
    file.add("case ServiceController.MimeTypes.png => Ok(renderToPng(v = r._2)).as(ServiceController.MimeTypes.png)")
    file.add("case ServiceController.MimeTypes.svg => Ok(renderToSvg(v = r._2)).as(ServiceController.MimeTypes.svg)")
    file.add("})", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add()
  }

  private[this] def addMutations(config: ExportConfiguration, file: ScalaFile, model: ExportModel, routesClass: String, viewHtmlPackage: String) = {
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
}
