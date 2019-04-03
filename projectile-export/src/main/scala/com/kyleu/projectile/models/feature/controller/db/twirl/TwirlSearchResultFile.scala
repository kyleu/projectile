package com.kyleu.projectile.models.feature.controller.db.twirl

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.file.TwirlFile

object TwirlSearchResultFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = TwirlFile(model.viewPackage(config), model.propertyName + "SearchResult")

    file.add(s"""@(model: ${model.fullClassPath(config)}, hit: String)<div class="search-result">""", 1)

    file.add(s"""<div class="right">""", 1)
    file.add(TwirlHelper.iconHtml(config = config, propertyName = model.propertyName, style = Some("font-size: 1rem;")) + " " + model.title)
    file.add(s"</div>", -1)

    file.add("<div>", 1)
    if (model.pkFields.isEmpty) {
      file.add("@model")
    } else {
      val cs = model.pkFields.map(f => "model." + f.propertyName)
      file.add(s"""<a href="@${TwirlHelper.routesClass(config, model)}.view(${cs.mkString(", ")})">${cs.map("@" + _).mkString(", ")}</a>""")
    }
    file.add("</div>", -1)

    file.add("<em>@hit</em>")

    val searches = model.searchFields.filterNot(model.pkFields.contains)
    if (searches.nonEmpty) {
      file.add("""<div style="margin-top: 12px;">""", 1)
      searches.foreach { f =>
        file.add(s"""<div class="chip">${f.title}: @model.${f.propertyName}</div>""")
      }
      file.add("</div>", -1)
    }

    file.add("</div>", -1)

    file
  }
}
