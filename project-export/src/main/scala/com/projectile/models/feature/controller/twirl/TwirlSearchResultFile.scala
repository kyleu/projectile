package com.projectile.models.feature.controller.twirl

import com.projectile.models.export.ExportModel
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.file.TwirlFile

object TwirlSearchResultFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = TwirlFile(config.applicationPackage ++ model.viewPackage, model.propertyName + "SearchResult")

    file.add(s"""@(model: ${model.fullClassPath(config)}, hit: String)<div class="search-result">""", 1)
    file.add(s"""<div class="right">${model.title}</div>""")
    file.add("<div>", 1)
    file.add(TwirlHelper.iconHtml(config, model.propertyName))
    if (model.pkFields.isEmpty) {
      file.add("@model")
    } else {
      val cs = model.pkFields.map(f => "model." + f.propertyName)
      file.add(s"""<a class="theme-text" href="@${TwirlHelper.routesClass(config, model)}.view(${cs.mkString(", ")})">${cs.map("@" + _).mkString(", ")}</a>""")
    }
    file.add("</div>", -1)
    file.add("<em>@hit</em>")
    file.add("</div>", -1)

    file
  }
}
