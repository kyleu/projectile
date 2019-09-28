package com.kyleu.projectile.models.feature.controller.db.twirl

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.feature.controller.db.ControllerHelper
import com.kyleu.projectile.models.output.CommonImportHelper
import com.kyleu.projectile.models.output.file.TwirlFile

object TwirlViewFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = TwirlFile(model.viewPackage(config), model.propertyName + "View")
    file.add(s"@(", 2)
    file.add(s"cfg: ${CommonImportHelper.getString(config, "UiConfig")},")
    file.add(s"model: ${model.fullClassPath(config)},")
    if (model.features(ModelFeature.Notes)) {
      file.add(s"notes: Seq[${CommonImportHelper.getString(config, "Note")}],")
    }
    if (model.features(ModelFeature.Audit)) {
      val modelPath = (config.systemPackage :+ "models" :+ "audit").mkString(".")
      file.add(s"auditRecords: Seq[$modelPath.AuditRecord],")
    }
    ControllerHelper.getFkConnections(config, model).foreach { fk =>
      file.add(s"${fk._1.propertyName}R: Option[${fk._2.modelPackage(config).mkString(".")}.${fk._2.className}],")
    }
    file.add("debug: Boolean")
    file.add(s")(", -2)
    val tdi = CommonImportHelper.get(config, "TraceData")._1.mkString(".")
    file.add(s"    implicit request: Request[AnyContent], session: Session, flash: Flash, td: $tdi.TraceData")
    addContent(config, model, file)
    file
  }

  private[this] def addContent(config: ExportConfiguration, model: ExportModel, file: TwirlFile) = {
    val imp = CommonImportHelper.get(config, "AugmentService")._1.mkString(".")
    val systemViewPkg = (config.systemViewPackage ++ Seq("html")).mkString(".")
    file.add(s""")@$systemViewPkg.layout.page(title = s"${model.title}", cfg = cfg, icon = Some(${model.iconRef(config)})) {""", 1)
    file.add(s"""@$imp.AugmentService.viewHeaders.augment(model, request.queryString, cfg)""")
    file.add("@com.kyleu.projectile.views.html.layout.card(None) {", 1)
    TwirlViewHelper.addButtons(config, model, file)
    TwirlViewHelper.addFields(config, model, file)
    file.add("}", -1)
    addModelFeatures(config, model, file)
    TwirlViewReferences.addReferences(config, model, file)
    file.add()
    file.add(s"@$systemViewPkg.components.includeScalaJs(debug)")
    file.add("}", -1)
  }

  private[this] def addModelFeatures(config: ExportConfiguration, model: ExportModel, file: TwirlFile) = if (model.pkFields.nonEmpty) {
    val imp = CommonImportHelper.get(config, "AugmentService")._1.mkString(".")
    file.add(s"""@$imp.AugmentService.viewDetails.augment(model, request.queryString, cfg)""")
    val modelPks = model.pkFields.map(f => s"model.${f.propertyName}").mkString(", ")
    if (model.features(ModelFeature.Notes)) {
      val viewPkg = (config.systemPackage ++ Seq("views", "html", "admin", "note")).mkString(".")
      file.add(s"""@$viewPkg.notes(cfg, notes, "${model.className}", "${model.title}", $modelPks)""")
    }
    if (model.features(ModelFeature.Audit)) {
      val viewPkg = (config.systemPackage ++ Seq("views", "html", "admin", "audit")).mkString(".")
      file.add(s"""@$viewPkg.auditRecords(auditRecords, "${model.className}", "${model.title}", $modelPks)""")
    }
  }
}
