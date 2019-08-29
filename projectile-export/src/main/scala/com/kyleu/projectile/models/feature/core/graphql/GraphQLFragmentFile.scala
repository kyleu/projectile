package com.kyleu.projectile.models.feature.core.graphql

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.feature.core.ModelHelper
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.util.StringUtils

object GraphQLFragmentFile {
  val includeDefaults = false

  def export(config: ExportConfiguration, model: ExportModel) = {
    val path = if (model.features(ModelFeature.Shared)) { OutputPath.SharedSource } else { OutputPath.ServerSource }
    val file = ScalaFile(path = path, dir = config.applicationPackage ++ model.pkg, key = model.className)

    config.addCommonImport(file, "JsonSerializers", "_")

    file.add(s"object ${model.className} {", 1)

    val objCount = GraphQLObjectWriter.writeObjects(s"fragment:${model.className}", config, file, model.fields)

    ModelHelper.addJson(config, file, model)

    model.source.foreach { src =>
      addContent(file, src)
    }

    file.add("}", -1)
    file.add()

    if (objCount > 0) {
      file.add(s"import ${model.className}._")
      file.add()
    }

    file.add(s"case class ${model.className}(", 2)
    GraphQLObjectHelper.addFields(config, file, model.fields)
    file.add(")", -2)

    file
  }

  private[this] def addContent(file: ScalaFile, src: String) = {
    file.add("val content = \"\"\"", 1)
    StringUtils.lines(src).foreach(l => file.add("|" + l))
    file.add("\"\"\".stripMargin.trim", -1)
  }
}
