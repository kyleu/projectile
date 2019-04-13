package com.kyleu.projectile.models.feature.controller.db.twirl

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.output.CommonImportHelper
import com.kyleu.projectile.models.output.file.TwirlFile

object TwirlFormFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = TwirlFile(model.viewPackage(config), model.propertyName + "Form")

    val viewPkg = (config.viewPackage :+ "html").mkString(".")

    val systemViewPkg = (config.systemViewPackage :+ "html").mkString(".")
    val sharedViewPkg = if (config.isNewUi) { (config.componentViewPackage :+ "html").mkString(".") } else { systemViewPkg + ".admin" }

    val extraArgs = "title: String, cancel: Call, act: Call, isNew: Boolean = false, debug: Boolean = false"

    val uc = CommonImportHelper.getString(config, "UiConfig")
    file.add(s"@(cfg: $uc, model: ${model.fullClassPath(config)}, $extraArgs)(")
    file.add(s"    implicit request: Request[AnyContent], session: Session, flash: Flash")

    file.add(s""")@$sharedViewPkg.layout.page(title, cfg) {""", 1)

    file.add(s"""<form id="form-edit-${model.propertyName}" action="@act" method="post">""", 1)
    if (config.isNewUi) { newContent(config, model, file) } else { originalContent(config, model, file) }
    file.add("</form>", -1)

    file.add("}", -1)

    file.add(s"@$viewPkg.components.includeScalaJs(debug)")

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

    file.add(s"""<script>$$(function() { new FormService('form-edit-${model.propertyName}'); })</script>""")

    file
  }

  private[this] def newContent(config: ExportConfiguration, model: ExportModel, file: TwirlFile) = {
    file.add("@com.kyleu.projectile.components.views.html.layout.card(None) {", 1)
    file.add(s"""<div class="right"><button type="submit" class="btn theme">@if(isNew) {Create} else {Save} ${model.title}</button></div>""")
    file.add("""<div class="right"><a href="@cancel" class="btn-flat cancel-link">Cancel</a></div>""")
    file.add("""<div class="clear"></div>""")
    table(config, model, file)
    file.add("}", -1)
  }

  private[this] def originalContent(config: ExportConfiguration, model: ExportModel, file: TwirlFile) = {
    file.add("""<div class="collection with-header">""", 1)
    file.add("<div class=\"collection-header\">", 1)
    file.add(s"""<div class="right"><button type="submit" class="btn theme">@if(isNew) {Create} else {Save} ${model.title}</button></div>""")
    file.add("""<div class="right"><a href="@cancel" class="btn-flat cancel-link">Cancel</a></div>""")
    file.add(s"""<h5>${TwirlHelper.iconHtml(config = config, propertyName = model.propertyName)} @title</h5>""")
    file.add("</div>", -1)
    file.add("<div class=\"collection-item\">", 1)
    table(config, model, file)
    file.add("</div>", -1)
    file.add("</div>", -1)
  }

  private[this] def table(config: ExportConfiguration, model: ExportModel, file: TwirlFile) = {
    file.add("<table>", 1)
    file.add("<tbody>", 1)
    model.fields.foreach { field =>
      val autocomplete = model.foreignKeys.find(_.references.forall(_.source == field.key)).map { fk =>
        fk -> config.getModel(fk.targetTable, s"foreign key ${fk.name}")
      }
      TwirlFormFields.fieldFor(config, model, field, file, autocomplete)
    }
    file.add("</tbody>", -1)
    file.add("</table>", -1)
  }
}
