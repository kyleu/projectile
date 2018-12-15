package com.kyleu.projectile.models.feature.wiki

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.MarkdownFile

object WikiListFiles {
  def export(config: ExportConfiguration) = {
    val file = MarkdownFile(OutputPath.WikiMarkdown, Seq("database"), "Database")
    file.addHeader("Database")

    file.addHeader(s"[Tables](DatabaseTables)", 2)
    config.models.sortBy(_.key).foreach { m =>
      file.add(s" - [${m.key}](DatabaseTable${m.className})")
    }
    file.add()

    file.addHeader(s"[Enums](DatabaseEnums)", 2)
    config.enums.sortBy(_.key).foreach { e =>
      file.add(s" - [${e.key}](DatabaseEnum${e.className})")
    }

    val tableFile = MarkdownFile(OutputPath.WikiMarkdown, Seq("database"), "DatabaseTables")
    tableFile.addHeader("Database Tables")
    config.models.sortBy(_.key).foreach { m =>
      tableFile.add(s" - [${m.key}](DatabaseTable${m.className})")
    }

    val enumFiles = if (config.enums.isEmpty) {
      Nil
    } else {
      val enumFile = MarkdownFile(OutputPath.WikiMarkdown, Seq("database"), "DatabaseEnums")
      enumFile.addHeader("Database Enums")
      config.enums.sortBy(_.key).foreach { e =>
        enumFile.add(s" - [${e.key}](DatabaseEnum${e.className})")
      }
      Seq(enumFile)
    }

    Seq(file, tableFile) ++ enumFiles
  }
}
