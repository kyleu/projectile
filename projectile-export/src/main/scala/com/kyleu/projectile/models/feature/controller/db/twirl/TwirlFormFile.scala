package com.kyleu.projectile.models.feature.controller.db.twirl

import com.kyleu.projectile.models.export.{ExportField, ExportModel}
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.output.CommonImportHelper
import com.kyleu.projectile.models.output.file.TwirlFile

object TwirlFormFile {
  def addScripts(config: ExportConfiguration, file: TwirlFile, model: ExportModel, systemViewPkg: String) = {
    file.add(s"@$systemViewPkg.components.includeScalaJs(debug)")
    if (model.fields.exists(field => model.foreignKeys.exists(_.references.forall(_.source == field.key)))) {
      file.add(s"@$systemViewPkg.components.includeAutocomplete(debug)")
    }
    val hasTagEditor = model.fields.exists(_.t match {
      case FieldType.ListType(_) | FieldType.SetType(_) => true
      case FieldType.MapType(_, _) | FieldType.TagsType => true
      case _ => false
    })
    if (hasTagEditor) {
      file.add(s"@$systemViewPkg.components.includeTagEditor(debug)")
    }
  }

  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = TwirlFile(model.viewPackage(config), model.propertyName + "Form")

    val systemViewPkg = (config.systemViewPackage :+ "html").mkString(".")

    val extraArgs = "title: String, cancel: Call, act: Call, isNew: Boolean = false, debug: Boolean = false"

    val uc = CommonImportHelper.getString(config, "UiConfig")
    file.add(s"@(cfg: $uc, model: ${model.fullClassPath(config)}, $extraArgs)(")
    file.add(s"    implicit request: Request[AnyContent], session: Session, flash: Flash")

    file.add(s""")@$systemViewPkg.layout.page(title = title, cfg = cfg, icon = Some(${model.iconRef(config)})) {""", 1)

    file.add(s"""<form id="form-edit-${model.propertyName}" action="@act" method="post">""", 1)
    addContent(config, model, file)
    file.add("</form>", -1)

    file.add("}", -1)

    addScripts(config, file, model, systemViewPkg)
    file.add(s"""<script>$$(function() { new FormService('form-edit-${model.propertyName}'); })</script>""")
    file
  }

  private[this] def addContent(config: ExportConfiguration, model: ExportModel, file: TwirlFile) = {
    file.add("@com.kyleu.projectile.views.html.layout.card(None) {", 1)
    file.add(s"""<div class="right"><button type="submit" class="btn @cfg.user.buttonColor">@if(isNew) {Create} else {Save} ${model.title}</button></div>""")
    file.add("""<div class="right"><a href="@cancel" class="btn-flat cancel-link">Cancel</a></div>""")
    file.add("""<div class="clear"></div>""")
    table(config, model, file)
    file.add("}", -1)
  }

  def autocomplete(config: ExportConfiguration, file: TwirlFile, model: ExportModel, field: ExportField) = {
    model.foreignKeys.find(_.references.forall(_.source == field.key)).map { fk =>
      fk -> config.getModel(fk.targetTable, s"foreign key ${fk.name}")
    }
  }

  private[this] def table(config: ExportConfiguration, model: ExportModel, file: TwirlFile) = {
    file.add("<table>", 1)
    file.add("<tbody>", 1)
    model.fields.foreach(field => TwirlFormFields.fieldFor(config, model, field, file, autocomplete(config, file, model, field)))
    file.add("</tbody>", -1)
    file.add("</table>", -1)
  }
}
