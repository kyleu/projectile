// scalastyle:off file.size.limit
package com.kyleu.projectile.models.feature.controller.db.twirl

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.{ExportModel, ExportModelReference}
import com.kyleu.projectile.models.output.file.TwirlFile

object TwirlViewReferences {
  def addReferences(config: ExportConfiguration, model: ExportModel, file: TwirlFile) = {
    if (ExportModelReference.validReferences(config, model).nonEmpty) {
      val args = model.pkFields.map(field => s"model.${field.propertyName}").mkString(", ")
      file.add()
      file.add("""<ul id="model-relations" class="collapsible" data-collapsible="expandable">""", 1)
      ExportModelReference.transformedReferences(config, model).foreach { r =>
        val src = r.src
        val srcField = r.tf
        val tgtField = r.f
        val relArgs = s"""data-table="${src.propertyName}" data-field="${srcField.propertyName}" data-singular="${src.title}" data-plural="${src.plural}""""
        val relAttrs = s"""id="relation-${src.propertyName}-${srcField.propertyName}" $relArgs"""
        val relUrl = TwirlHelper.routesClass(config, src) + s".by${srcField.className}(model.${tgtField.propertyName}, limit = Some(5), embedded = true)"
        file.add(s"""<li $relAttrs data-url="@$relUrl">""", 1)
        file.add("""<div class="collapsible-header">""", 1)
        file.add(TwirlHelper.iconHtml(config = config, propertyName = src.propertyName, provided = src.provided))
        file.add(s"""<span class="title">${src.plural}</span>&nbsp;by ${srcField.title}""")
        file.add(s"""<span class="badge" style="display: none;">""", 1)
        def icon(s: String) = s"""<i class="material-icons" style="margin-right: 0;">$s</i>"""

        val editUrl = TwirlHelper.routesClass(config, src) + s".by${srcField.className}BulkForm(model.${tgtField.propertyName})"
        file.add(s"""<a class="bulk-edit-link" title="Bulk edit" href="@$editUrl">${icon("edit")}</a>""")
        val linkUrl = TwirlHelper.routesClass(config, src) + s".by${srcField.className}(model.${tgtField.propertyName})"
        file.add(s"""<a class="view-relation-link" title="View these ${src.plural}" href="@$linkUrl">${icon("insert_link")}</a>""")

        file.add(s"""</span>""", -1)
        file.add("</div>", -1)
        file.add("""<div class="collapsible-body"><span>Loading...</span></div>""")
        file.add("</li>", -1)
      }
      file.add("</ul>", -1)
      // file.add(s"@${(config.systemViewPackage ++ Seq("html", "components")).mkString(".")}.includeScalaJs(debug)")
      file.add(s"""<script>$$(function() { new RelationService('@${TwirlHelper.routesClass(config, model)}.relationCounts($args)') });</script>""")
    }
  }
}
