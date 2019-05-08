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
    file.add(s"    implicit request: Request[AnyContent], session: Session, flash: Flash")
    if (config.isNewUi) { newContent(config, model, file) } else { originalContent(config, model, file) }
    file
  }

  private[this] def newContent(config: ExportConfiguration, model: ExportModel, file: TwirlFile) = {
    val systemViewPkg = (config.systemViewPackage ++ Seq("html")).mkString(".")
    file.add(s""")@$systemViewPkg.layout.page(s"${model.title}", cfg) {""", 1)
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

  private[this] def originalContent(config: ExportConfiguration, model: ExportModel, file: TwirlFile) = {
    val systemViewPkg = (config.systemViewPackage ++ Seq("html")).mkString(".")
    val toInterp = model.pkFields.map(c => "${model." + c.propertyName + "}").mkString(", ")
    file.add(s""")@$systemViewPkg.admin.layout.page(s"${model.title} [$toInterp]", cfg) {""", 1)
    file.add("""<div class="collection with-header">""", 1)
    file.add("<div class=\"collection-header\">", 1)
    TwirlViewHelper.addButtons(config, model, file)
    file.add("<h5>", 1)
    val modelIcon = TwirlHelper.iconHtml(config = config, propertyName = model.propertyName)
    file.add(s"""<a href="@${TwirlHelper.routesClass(config, model)}.list()">$modelIcon</a>""")
    val toTwirl = model.pkFields.map(c => "@model." + c.propertyName).mkString(", ")
    file.add(s"""${model.title} [$toTwirl]""")
    file.add("</h5>", -1)
    file.add("</div>", -1)
    file.add("<div class=\"collection-item\">", 1)
    TwirlViewHelper.addFields(config, model, file)
    file.add("</div>", -1)
    addModelFeatures(config, model, file)
    file.add("</div>", -1)
    TwirlViewHelper.addReferences(config, model, file)
    file.add()
    file.add(s"@$systemViewPkg.components.includeScalaJs(debug)")
    file.add("}", -1)
  }

  private[this] def addModelFeatures(config: ExportConfiguration, model: ExportModel, file: TwirlFile) = if (model.pkFields.nonEmpty) {
    if (config.project.flags("augmented")) {
      val imp = CommonImportHelper.get(config, "AugmentService")._1.mkString(".")
      file.add()
      file.add(s"""@$imp.AugmentService.augmentModel(model, request.queryString)""")
    }
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
