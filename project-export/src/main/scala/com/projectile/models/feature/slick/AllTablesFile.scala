package com.projectile.models.feature.slick

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.ModelFeature
import com.projectile.models.output.OutputPath
import com.projectile.models.output.file.ScalaFile

object AllTablesFile {
  def export(config: ExportConfiguration) = {
    val file = ScalaFile(path = OutputPath.ServerSource, dir = config.applicationPackage ++ List("models", "table"), key = "AllTables")

    val ms = config.models.filter(_.features(ModelFeature.Slick)).map(m => m.slickPackage -> (m.className + "Table"))
    val es = Nil // config.enums.filter(_.features(EnumFeature.Slick)).map(e => e.slickPackage -> (e.className + "ColumnType"))
    val schema = (ms ++ es).map(x => (config.applicationPackage ++ x._1 :+ x._2 :+ "query" :+ "schema").mkString(".")).sorted.mkString(",\n    ")

    config.addCommonImport(file, "SlickQueryService", "imports", "_")
    file.add(s"object AllTables {", 1)
    file.add(s"val schema = Seq(", 1)
    file.add(schema)
    file.add(")", -1)
    file.add("}", -1)

    file
  }
}
