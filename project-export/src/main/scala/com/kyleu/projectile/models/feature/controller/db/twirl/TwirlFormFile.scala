package com.kyleu.projectile.models.feature.controller.db.twirl

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.CommonImportHelper
import com.kyleu.projectile.models.output.file.TwirlFile

object TwirlFormFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = TwirlFile(model.viewPackage(config), model.propertyName + "Form")

    val modelPkg = (config.applicationPackage :+ "models").mkString(".")
    val viewPkg = (config.viewPackage :+ "html").mkString(".")

    val su = CommonImportHelper.getString(config, "SystemUser")
    file.add(s"@(user: $su, model: ${model.fullClassPath(config)}, title: String, cancel: Call, act: Call, isNew: Boolean = false, debug: Boolean = false)(")
    val td = config.utilitiesPackage.mkString(".") + ".tracing.TraceData"
    file.add(s"    implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: $td")
    file.add(s""")@$viewPkg.admin.layout.page(user, "explore", title) {""", 1)

    file.add(s"""<form id="form-edit-${model.propertyName}" action="@act" method="post">""", 1)
    file.add("""<div class="collection with-header">""", 1)

    file.add("<div class=\"collection-header\">", 1)
    file.add(s"""<div class="right"><button type="submit" class="btn theme">@if(isNew) {Create} else {Save} ${model.title}</button></div>""")
    file.add(s"""<div class="right"><a href="@cancel" class="theme-text cancel-link">Cancel</a></div>""")
    file.add(s"""<h5>${TwirlHelper.iconHtml(config, model.propertyName)} @title</h5>""")
    file.add("</div>", -1)

    file.add("<div class=\"collection-item\">", 1)
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
    file.add("</div>", -1)

    file.add("</div>", -1)
    file.add("</form>", -1)

    file.add("}", -1)

    file.add(s"@$viewPkg.components.includeScalaJs(debug)")
    // file.add(s"@${config.corePrefix}com.kyleu.projectile.web.views.html.components.includeAutocomplete(debug)")
    file.add(s"""<script>$$(function() { new FormService('form-edit-${model.propertyName}'); })</script>""")

    file
  }
}
