package models.output.feature.wiki

import models.export.ExportEnum
import models.export.config.ExportConfiguration
import models.output.OutputPath
import models.output.file.MarkdownFile

object WikiEnumFile {
  def export(config: ExportConfiguration, e: ExportEnum) = {
    val file = MarkdownFile(OutputPath.WikiMarkdown, "database" +: e.pkg, "DatabaseEnum" + e.className)
    file.addHeader(e.name)
    e.values.foreach(v => file.add(s" - $v"))
    file
  }
}
