package com.kyleu.projectile.models.feature.core.graphql

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.util.StringUtils

object GraphQLOperationFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    exportOp(config = config, model = model, useQuery = true)
  }

  def exportOp(config: ExportConfiguration, model: ExportModel, useQuery: Boolean) = {
    val path = if (model.features(ModelFeature.Shared)) { OutputPath.SharedSource } else { OutputPath.ServerSource }
    val file = ScalaFile(path = path, dir = config.applicationPackage ++ model.pkg, key = model.className)

    config.addCommonImport(file, "JsonSerializers", "_")
    if (useQuery) {
      config.addCommonImport(file, "TextQuery")
    }

    file.add(s"object ${model.className} {", 1)

    GraphQLObjectHelper.addArguments(config, file, model.arguments)

    GraphQLObjectHelper.writeObjects(s"operation:${model.className}", config, file, model.fields)

    file.add("object Data {", 1)
    file.add("implicit val jsonDecoder: Decoder[Data] = deriveDecoder")
    file.add("}", -1)
    file.add("case class Data(", 2)
    GraphQLObjectHelper.addFields(config, file, model.fields)
    file.add(")", -2)
    file.add()
    if (useQuery) {
      model.source match {
        case None => file.add(s"""val query: TextQuery[Data] = new TextQuery[Data]("${model.className}")""")
        case Some(src) =>
          file.add(s"""val query: TextQuery[Data] = new TextQuery[Data]("${model.className}") {""", 1)
          addContent(file, src)
          file.add("}", -1)
      }
    } else {
      file.add(s"""val name = "${model.className}"""")
      file.add("""def getData(json: Json): Option[Data] = if (json.isNull) { None } else { Some(extract[Data](json)) }""")
      model.source.foreach(src => addContent(file, src))
    }

    file.add("}", -1)
    file.add()

    file
  }

  private[this] def addContent(file: ScalaFile, src: String) = {
    file.add("override val content = \"\"\"", 1)
    StringUtils.lines(src).foreach(l => file.add("|" + l))
    file.add("\"\"\".stripMargin.trim", -1)
  }
}
