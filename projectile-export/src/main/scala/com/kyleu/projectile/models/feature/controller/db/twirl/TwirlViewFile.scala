package com.kyleu.projectile.models.feature.controller.db.twirl

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.output.CommonImportHelper
import com.kyleu.projectile.models.output.file.TwirlFile

object TwirlViewFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = TwirlFile(model.viewPackage(config), model.propertyName + "View")
    val modelPath = (config.applicationPackage :+ "models").mkString(".")

    val finalArgs = s"cfg: ${CommonImportHelper.getString(config, "UiConfig")}"
    val audits = if (model.features(ModelFeature.Audit)) { s", auditRecords: Seq[$modelPath.audit.AuditRecord]" } else { "" }
    val notes = if (model.features(ModelFeature.Notes)) { s", notes: Seq[${CommonImportHelper.getString(config, "Note")}]" } else { "" }

    file.add(s"@($finalArgs, model: ${model.fullClassPath(config)}$notes$audits, debug: Boolean)(")
    file.add(s"    implicit request: Request[AnyContent], session: Session, flash: Flash")

    if (config.isNewUi) { newContent(config, model, file) } else { originalContent(config, model, file) }

    file
  }

  private[this] def newContent(config: ExportConfiguration, model: ExportModel, file: TwirlFile) = {
    val systemViewPkg = (config.systemViewPackage ++ Seq("html", "admin")).mkString(".")
    val sharedViewPkg = if (config.isNewUi) { (config.componentViewPackage :+ "html").mkString(".") } else { systemViewPkg }
    file.add(s""")@$sharedViewPkg.layout.page(s"${model.title}", cfg) {""", 1)

    file.add("@com.kyleu.projectile.components.views.html.layout.card(None) {", 1)
    TwirlViewHelper.addButtons(config, model, file)
    TwirlViewHelper.addFields(config, model, file)
    file.add("}", -1)
    addModelFeatures(config, model, file)

    TwirlViewHelper.addReferences(config, model, file)
    file.add("}", -1)
  }

  private[this] def originalContent(config: ExportConfiguration, model: ExportModel, file: TwirlFile) = {
    val systemViewPkg = (config.systemViewPackage ++ Seq("html", "admin")).mkString(".")
    val sharedViewPkg = if (config.isNewUi) { (config.componentViewPackage :+ "html").mkString(".") } else { systemViewPkg }
    val toInterp = model.pkFields.map(c => "${model." + c.propertyName + "}").mkString(", ")
    file.add(s""")@$sharedViewPkg.layout.page(s"${model.title} [$toInterp]", cfg) {""", 1)

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
    file.add("}", -1)
  }

  private[this] def addModelFeatures(config: ExportConfiguration, model: ExportModel, file: TwirlFile) = if (model.pkFields.nonEmpty) {
    val viewPkg = (config.viewPackage ++ Seq("html", "admin")).mkString(".")
    val modelPks = model.pkFields.map(f => s"model.${f.propertyName}").mkString(", ")
    if (config.project.flags("augmented")) {
      val imp = CommonImportHelper.get(config, "AugmentService")._1.mkString(".")
      file.add()
      file.add(s"""@$imp.AugmentService.augmentModel(model, request.queryString)""")
    }
    if (model.features(ModelFeature.Notes)) {
      file.add()
      file.add(s"""@$viewPkg.note.notes(notes, "${model.propertyName}", "${model.title}", $modelPks)""")
    }
    if (model.features(ModelFeature.Audit)) {
      file.add()
      file.add(s"""@$viewPkg.audit.auditRecords(auditRecords, "${model.propertyName}", "${model.title}", $modelPks)""")
    }
  }
}
