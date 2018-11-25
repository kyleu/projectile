package com.projectile.models.feature.wiki

import com.projectile.models.export.ExportEnum
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.OutputPath
import com.projectile.models.output.file.MarkdownFile

object WikiEnumFile {
  def export(config: ExportConfiguration, e: ExportEnum) = {
    val file = MarkdownFile(OutputPath.WikiMarkdown, "database" +: e.pkg, "DatabaseEnum" + e.className)
    file.addHeader(e.key)
    e.values.foreach(v => file.add(s" - $v"))
    file
  }
}
