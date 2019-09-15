package com.kyleu.projectile.models.feature.controller.db.twirl

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.CommonImportHelper
import com.kyleu.projectile.models.output.file.TwirlFile

object TwirlBulkEditFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = TwirlFile(model.viewPackage(config), model.propertyName + "BulkForm")

    val systemViewPkg = (config.systemViewPackage :+ "html").mkString(".")

    val uc = CommonImportHelper.getString(config, "UiConfig")
    file.add(s"@(cfg: $uc, modelSeq: Seq[${model.fullClassPath(config)}], act: Call, debug: Boolean)(")
    file.add(s"    implicit flash: Flash")

    file.add(s""")@$systemViewPkg.layout.page(title = "Bulk Edit", cfg = cfg, icon = Some(${model.iconRef(config)})) {""", 1)
    file.add(s"""<form id="form-edit-${model.propertyName}" action="@act" method="post">""", 1)
    val pkString = model.pkFields.map("m." + _.propertyName).mkString(""" + "||" + """)
    file.add(s"""<input type="hidden" class="primaryKeys" name="primaryKeys" value="@modelSeq.map(m => $pkString).mkString("//")" />""")
    addSelections(config, file, model)
    addForm(config, file, model)
    file.add("</form>", -1)
    file.add("}", -1)
    TwirlFormFile.addScripts(config, file, model, systemViewPkg)
    file.add(s"<script>", 1)
    file.add(s"""$$(function() { new FormService('form-edit-${model.propertyName}'); });""")
    file.add(s"""$$(function() { new BulkEditService('form-edit-${model.propertyName}'); });""")
    file.add(s"</script>", -1)
    file
  }

  def addSelections(config: ExportConfiguration, file: TwirlFile, model: ExportModel) = {
    file.add(s"""@com.kyleu.projectile.views.html.layout.card(Some("Selected ${model.plural}")) {""", 1)
    val saveTitle = s"Save <span>@modelSeq.size</span> ${model.plural}"
    file.add(s"""<div class="right"><button type="submit" class="btn @cfg.user.buttonColor">$saveTitle</button></div>""")
    file.add("""<div class="right"><a href="" onclick="window.history.go(-1);return false;" class="btn-flat cancel-link">Cancel</a></div>""")
    file.add("""<div class="clear"></div>""")

    file.add("<table>", 1)
    file.add("<thead>", 1)
    file.add("<tr>", 1)
    model.summaryFields.foreach(f => file.add(s"<th>${f.title}</th>"))
    file.add("</tr>", -1)
    file.add("</thead>", -1)
    file.add("<tbody>", 1)
    file.add("@modelSeq.map { model =>", 1)
    val icon = "<i class='material-icons'>close</i>"
    val pk = model.pkFields.map(f => s"$${model.${f.propertyName}}").mkString("---").replaceAllLiterally("'", "")
    val extra = s"<td style='text-align: right;'><a class='remove-pk' data-pk='$pk' href='' title='Remove from editing'>$icon</a></td>"
    file.add(s"""@${model.propertyName}DataRow(model, Some(Html(s"$extra")))""")
    file.add("}", -1)
    file.add("</tbody>", -1)
    file.add("</table>", -1)

    file.add("}", -1)
  }

  def addForm(config: ExportConfiguration, file: TwirlFile, model: ExportModel) = {
    file.add("""@com.kyleu.projectile.views.html.layout.card(None) {""", 1)
    if (model.nonPkFields.isEmpty) {
      file.add("<div>I'm sorry, this table doesn't have any fields that can be bulk edited</div>")
    } else {
      file.add("<table>", 1)
      file.add("<tbody>", 1)
      model.nonPkFields.foreach { field =>
        val autocomplete = TwirlFormFile.autocomplete(config, file, model, field)
        TwirlFormFields.fieldFor(config, model, field, file, autocomplete, isNewOverride = Some("false"), vOverride = Some("None"))
      }
      file.add("</tbody>", -1)
      file.add("</table>", -1)

    }
    file.add("}", -1)
  }
}
