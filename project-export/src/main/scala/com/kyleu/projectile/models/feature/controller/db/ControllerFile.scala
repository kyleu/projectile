package com.kyleu.projectile.models.feature.controller.db

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile

object ControllerFile {

  private[this] def addMutations(file: ScalaFile, model: ExportModel, routesClass: String, viewHtmlPackage: String) = if (!model.readOnly) {
    file.add("""def createForm = withSession("create.form", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"val cancel = $routesClass.list()")
    file.add(s"val call = $routesClass.create()")
    file.add(s"Future.successful(Ok($viewHtmlPackage.${model.propertyName}Form(", 1)
    file.add(s"""request.identity, ${model.className}.empty(), "New ${model.title}", cancel, call, isNew = true, debug = app.config.debug""")
    file.add(")))", -1)
    file.add("}", -1)
    file.add()
    file.add("""def create = withSession("create", admin = true) { implicit request => implicit td =>""", 1)
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

  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ScalaFile(path = OutputPath.ServerSource, config.applicationPackage ++ model.controllerPackage, model.className + "Controller")
    val viewHtmlPackage = (config.applicationPackage ++ model.viewHtmlPackage).mkString(".")
    val routesClass = (config.applicationPackage ++ model.routesPackage :+ (model.className + "Controller")).mkString(".")

    file.addImport(config.applicationPackage ++ model.modelPackage, model.className)
    config.addCommonImport(file, "Application")
    config.addCommonImport(file, "ServiceController")
    config.addCommonImport(file, "AuditRecordService")

    config.addCommonImport(file, "OrderBy")

    config.addCommonImport(file, "JsonSerializers", "_")
    config.addCommonImport(file, "DateUtils")
    config.addCommonImport(file, "ReftreeUtils", "_")

    file.addImport(Seq("scala", "concurrent"), "Future")
    file.addImport(Seq("play", "api", "http"), "MimeTypes")

    file.addImport(config.applicationPackage ++ model.servicePackage, model.className + "Service")
    file.addImport(config.applicationPackage ++ model.modelPackage, model.className + "Result")

    if (model.propertyName != "audit") {
      file.addMarker("string-search", model.key)
      model.pkFields match {
        case sole :: Nil => sole.t match {
          case FieldType.UuidType => file.addMarker("uuid-search", model.key)
          case FieldType.IntegerType => file.addMarker("int-search", model.key)
          case _ => // noop
        }
        case _ => // noop
      }
    }

    file.add("@javax.inject.Singleton")
    file.add(s"class ${model.className}Controller @javax.inject.Inject() (", 2)
    ControllerReferences.refServiceArgs(config, model, file) match {
      case ref if ref.trim.isEmpty => file.add(s"override val app: Application, svc: ${model.className}Service, auditRecordSvc: AuditRecordService")
      case ref =>
        file.add(s"override val app: Application, svc: ${model.className}Service, auditRecordSvc: AuditRecordService,")
        file.add(ref)
    }
    file.add(s") extends ServiceController(svc) {", -2)
    file.indent()
    file.add("import app.contexts.webContext")
    file.add()
    addMutations(file, model, routesClass, viewHtmlPackage)
    val listArgs = "orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None"
    file.add(s"""def list(q: Option[String], $listArgs) = {""", 1)
    file.add("""withSession("list", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"val startMs = DateUtils.nowMillis")
    file.add("val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq")
    file.add("searchWithCount(q, orderBys, limit, offset).map(r => renderChoice(t) {", 1)

    file.add(s"case MimeTypes.HTML => Ok($viewHtmlPackage.${model.propertyName}List(", 1)
    file.add("request.identity, Some(r._1), r._2, q, orderBy, orderAsc, limit.getOrElse(100), offset.getOrElse(0)")
    file.add("))", -1)
    file.add(s"case MimeTypes.JSON => Ok(${model.className}Result.fromRecords(q, Nil, orderBys, limit, offset, startMs, r._1, r._2).asJson)")
    file.add(s"""case ServiceController.MimeTypes.csv => csvResponse("${model.className}", svc.csvFor(r._1, r._2))""")
    file.add(s"case ServiceController.MimeTypes.png => Ok(renderToPng(v = r._2)).as(ServiceController.MimeTypes.png)")
    file.add(s"case ServiceController.MimeTypes.svg => Ok(renderToSvg(v = r._2)).as(ServiceController.MimeTypes.svg)")
    file.add("})", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add()
    file.add(s"""def autocomplete(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int]) = {""", 1)
    file.add("""withSession("autocomplete", admin = true) { implicit request => implicit td =>""", 1)
    file.add("val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq")
    file.add("search(q, orderBys, limit, None).map(r => Ok(r.map(_.toSummary).asJson))")
    file.add("}", -1)
    file.add("}", -1)
    ControllerHelper.writeForeignKeys(config, model, file)
    ControllerHelper.writePks(config, model, file, viewHtmlPackage, routesClass)
    ControllerReferences.write(config, model, file)
    file.add("}", -1)
    file
  }
}
