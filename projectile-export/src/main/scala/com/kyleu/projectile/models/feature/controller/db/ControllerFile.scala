// scalastyle:off file.size.limit
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
    ControllerMutations.addImports(config, file, model)
    model.globalSearchFields.foreach { gsf =>
      val m = model.key -> gsf.propertyName
      gsf.t match {
        case FieldType.StringType => file.addMarkers("string-search", m)
        case FieldType.UuidType => file.addMarkers("uuid-search", m)
        case FieldType.IntegerType => file.addMarkers("int-search", m)
        case FieldType.LongType => file.addMarkers("long-search", m)
        case FieldType.SerialType => file.addMarkers("long-search", m)
        case _ => // noop
      }
    }

    file.add("@javax.inject.Singleton")
    file.add(s"class ${model.className}Controller @javax.inject.Inject() (", 2)
    if (model.features(ModelFeature.Notes)) { config.addCommonImport(file, "NoteService") }
    val extraSvcs = {
      val n = if (model.features(ModelFeature.Notes)) { ", noteSvc: NoteService" } else { "" }
      val a = if (model.features(ModelFeature.Audit)) { ", auditSvc: AuditService" } else { "" }
      val r = ""
      n + a + r
    }

    ControllerReferences.serviceArgs(config, model, file) match {
      case svcs if svcs.isEmpty => file.add(s"override val app: Application, svc: ${model.className}Service$extraSvcs")
      case svcs =>
        file.add(s"override val app: Application, svc: ${model.className}Service$extraSvcs,")
        file.add(svcs.mkString(", "))
    }
    val controller = if (model.features(ModelFeature.Auth)) { "ServiceAuthController" } else { "ServiceController" }
    file.add(s")(implicit ec: ExecutionContext) extends $controller(svc) {", -2)
    file.indent()

    config.addCommonImport(file, "PermissionService")
    val firstPkg = model.pkg.headOption.getOrElse("system")
    val ico = s"""Some(${(config.applicationPackage :+ "models").mkString(".")}.template.Icons.${model.propertyName})"""
    file.add(s"""PermissionService.registerModel("$firstPkg", "${model.className}", "${model.title}", $ico, "view", "edit")""")
    model.defaultOrder match {
      case Some(o) => file.add(s"""private[this] val defaultOrderBy = Some("${model.getField(o.col).propertyName}" -> ${o.dir.asBool})""")
      case None => file.add(s"""private[this] val defaultOrderBy = None""")
    }
    file.add()
    addListAction(config, file, model, viewHtmlPackage)
    file.add("""def autocomplete(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int]) = {""", 1)
    file.add(s"""withSession("autocomplete", ${model.perm("view")}) { implicit request => implicit td =>""", 1)
    file.add("val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq")
    file.add("search(q, orderBys, limit, None).map(r => Ok(r.map(_.toSummary).asJson))")
    file.add("}", -1)
    file.add("}", -1)
    ControllerHelper.writePks(config, model, file, viewHtmlPackage, routesClass)
    ControllerMutations.addMutations(config, file, model, routesClass, viewHtmlPackage)
    ControllerMutations.addBulkEdit(config, file, model)
    ControllerReferences.writeForeignKeys(config, model, file, viewHtmlPackage)
    ControllerReferences.write(config, model, file)
    file.add("}", -1)
    file
  }

  private[this] def addListAction(config: ExportConfiguration, file: ScalaFile, model: ExportModel, viewHtmlPackage: String) = {
    val routesClass = (model.routesPackage(config) :+ (model.className + "Controller")).mkString(".")
    val listArgs = "orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None"
    file.add(s"""def list(q: Option[String], $listArgs) = {""", 1)
    file.add(s"""withSession("list", ${model.perm("view")}) { implicit request => implicit td =>""", 1)
    file.add("val startMs = DateUtils.nowMillis")
    file.add("val orderBys = OrderBy.forVals(orderBy, orderAsc, defaultOrderBy).toSeq")
    file.add("searchWithCount(q, orderBys, limit, offset).map(r => renderChoice(t) {", 1)
    file.add(s"case MimeTypes.HTML => r._2.toList match {", 1)
    val redirArgs = model.pkFields.map(f => "model." + f.propertyName).mkString(", ")
    file.add(s"case model :: Nil if q.nonEmpty => Redirect($routesClass.view($redirArgs))")
    val cfgArg = s"""app.cfg(u = Some(request.identity), "${model.firstPackage}", "${model.key}")"""
    val args = s"$cfgArg, Some(r._1), r._2, q, orderBys.headOption.map(_.col), orderBys.exists(_.dir.asBool), limit.getOrElse(100), offset.getOrElse(0)"
    file.add(s"case _ => Ok($viewHtmlPackage.${model.propertyName}List($args))")
    file.add("}", -1)
    file.add(s"case MimeTypes.JSON => Ok(${model.className}Result.fromRecords(q, Nil, orderBys, limit, offset, startMs, r._1, r._2).asJson)")
    file.add(s"""case BaseController.MimeTypes.csv => csvResponse("${model.className}", svc.csvFor(r._1, r._2))""")
    file.add("})", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add()
  }
}
