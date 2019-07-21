package com.kyleu.projectile.models.feature.controller.db.twirl

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.output.CommonImportHelper
import com.kyleu.projectile.models.output.file.TwirlFile

object TwirlViewFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = TwirlFile(model.viewPackage(config), model.propertyName + "View")
    val finalArgs = s"cfg: ${CommonImportHelper.getString(config, "UiConfig")}"
    val modelPath = (config.systemPackage :+ "models" :+ "audit").mkString(".")
    val audits = if (model.features(ModelFeature.Audit)) { s", auditRecords: Seq[$modelPath.AuditRecord]" } else { "" }
    val notes = if (model.features(ModelFeature.Notes)) { s", notes: Seq[${CommonImportHelper.getString(config, "Note")}]" } else { "" }
    file.add(s"@($finalArgs, model: ${model.fullClassPath(config)}$notes$audits, debug: Boolean)(")
    val tdi = CommonImportHelper.get(config, "TraceData")._1.mkString(".")
    file.add(s"    implicit request: Request[AnyContent], session: Session, flash: Flash, td: $tdi.TraceData")
    addContent(config, model, file)
    file
  }

  private[this] def addContent(config: ExportConfiguration, model: ExportModel, file: TwirlFile) = {
    val systemViewPkg = (config.systemViewPackage ++ Seq("html")).mkString(".")
    val icon = s"${(config.applicationPackage :+ "models" :+ "template").mkString(".")}.Icons.${model.propertyName}"
    file.add(s""")@$systemViewPkg.layout.page(title = s"${model.title}", cfg = cfg, icon = Some($icon)) {""", 1)
    file.add("@com.kyleu.projectile.views.html.layout.card(None) {", 1)
    TwirlViewHelper.addButtons(config, model, file)
    TwirlViewHelper.addFields(config, model, file)
    file.add("}", -1)
    addModelFeatures(config, model, file)
    TwirlViewHelper.addReferences(config, model, file)
    file.add()
    file.add(s"@$systemViewPkg.components.includeScalaJs(debug)")
    file.add("}", -1)
  }

  private[this] def addModelFeatures(config: ExportConfiguration, model: ExportModel, file: TwirlFile) = if (model.pkFields.nonEmpty) {
    val imp = CommonImportHelper.get(config, "AugmentService")._1.mkString(".")
    file.add()
    file.add(s"""@$imp.AugmentService.views.augment(model, request.queryString, cfg)""")
    val modelPks = model.pkFields.map(f => s"model.${f.propertyName}").mkString(", ")
    if (model.features(ModelFeature.Notes)) {
      file.add()
      val viewPkg = (config.systemPackage ++ Seq("views", "html", "admin", "note")).mkString(".")
      file.add(s"""@$viewPkg.notes(cfg, notes, "${model.className}", "${model.title}", $modelPks)""")
    }
    if (model.features(ModelFeature.Audit)) {
      file.add()
      val viewPkg = (config.systemPackage ++ Seq("views", "html", "admin", "audit")).mkString(".")
      file.add(s"""@$viewPkg.auditRecords(auditRecords, "${model.className}", "${model.title}", $modelPks)""")
    }
  }
}
