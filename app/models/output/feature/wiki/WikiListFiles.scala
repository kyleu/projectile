package models.output.feature.wiki

import models.export.config.ExportConfiguration
import models.output.OutputPath
import models.output.file.MarkdownFile

object WikiListFiles {
  def export(config: ExportConfiguration) = {
    val file = MarkdownFile(OutputPath.WikiMarkdown, Seq("database"), "Database")
    file.addHeader("Database")

    file.addHeader(s"[Tables](DatabaseTables)", 2)
    config.models.foreach { m =>
      file.add(s" - [${m.name}](DatabaseTable${m.className})")
    }
    file.add()

    file.addHeader(s"[Enums](DatabaseEnums)", 2)
    config.enums.foreach { e =>
      file.add(s" - [${e.name}](DatabaseEnum${e.className})")
    }

    val tableFile = MarkdownFile(OutputPath.WikiMarkdown, Seq("database"), "DatabaseTables")
    tableFile.addHeader("Database Tables")
    config.models.foreach { m =>
      tableFile.add(s" - [${m.name}](DatabaseTable${m.className})")
    }

    val enumFiles = if (config.enums.isEmpty) {
      Nil
    } else {
      val enumFile = MarkdownFile(OutputPath.WikiMarkdown, Seq("database"), "DatabaseEnums")
      enumFile.addHeader("Database Enums")
      config.enums.foreach { e =>
        enumFile.add(s" - [${e.name}](DatabaseEnum${e.className})")
      }
      Seq(enumFile)
    }

    Seq(file, tableFile) ++ enumFiles
  }
}
