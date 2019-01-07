package com.kyleu.projectile.models.feature.wiki

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.MarkdownFile

object WikiModelFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = MarkdownFile(OutputPath.WikiMarkdown, "database" +: model.pkg, "DatabaseTable" + model.className)
    file.addHeader(model.key)

    file.addHeader("Columns", 2)
    MarkdownFile.table(file, Seq(
      ('l', 30, "Name"), ('l', 20, "Type"), ('l', 8, "NotNull"), ('l', 8, "Unique"), ('l', 10, "Indexed"), ('l', 20, "Default")
    ), model.fields.map { f =>
      Seq(f.key, f.t.toString, f.required.toString, f.unique.toString, f.indexed.toString, f.defaultValue.getOrElse(""))
    })
    file.add()

    if (model.references.nonEmpty) {
      file.addHeader("References", 2)
      MarkdownFile.table(file, Seq(('l', 30, "Name"), ('l', 20, "Target"), ('l', 40, "Table"), ('l', 20, "Column")), model.references.sortBy(_.name).map { r =>
        val src = config.getModel(r.srcTable, "wiki")
        Seq(r.name, r.tgt, MarkdownFile.link(r.srcTable, s"DatabaseTable${src.className}"), r.srcCol)
      })
    }

    file
  }
}
