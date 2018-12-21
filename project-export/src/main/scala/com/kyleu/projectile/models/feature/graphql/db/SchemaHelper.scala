package com.kyleu.projectile.models.feature.graphql.db

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.{ExportField, ExportModel}
import com.kyleu.projectile.models.feature.graphql.ExportFieldGraphQL
import com.kyleu.projectile.models.output.file.ScalaFile

object SchemaHelper {
  def addImports(config: ExportConfiguration, file: ScalaFile) = {
    config.addCommonImport(file, "GraphQLUtils", "_")
    config.addCommonImport(file, "GraphQLContext")
    config.addCommonImport(file, "GraphQLSchemaHelper")
    file.addImport(Seq("sangria", "schema"), "_")
  }

  def addPrimaryKey(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = if (model.pkFields.nonEmpty) {
    model.pkFields.foreach(_.addImport(config, file, model.pkg))
    file.addImport(Seq("sangria", "execution", "deferred"), "HasId")
    val method = if (model.pkFields.lengthCompare(1) == 0) {
      model.pkFields.headOption.map(f => "_." + f.propertyName).getOrElse(throw new IllegalStateException())
    } else {
      "x => (" + model.pkFields.map(f => "x." + f.propertyName).mkString(", ") + ")"
    }
    val hasId = s"HasId[${model.className}, ${model.pkType(config)}]"
    file.add(s"implicit val ${model.propertyName}PrimaryKeyId: $hasId = $hasId($method)")
    file.add(s"private[this] def getByPrimaryKeySeq(c: GraphQLContext, idSeq: Seq[${model.pkType(config)}]) = {", 1)
    file.add(s"c.${model.serviceReference}.getByPrimaryKeySeq(c.creds, idSeq)(c.trace)")
    file.add("}", -1)
    file.addImport(Seq("sangria", "execution", "deferred"), "Fetcher")
    val fetcherName = s"${model.propertyName}ByPrimaryKeyFetcher"
    file.addMarker("fetcher", (model.modelPackage(config) :+ (model.className + "Schema")).mkString(".") + "." + fetcherName)
    file.add(s"val $fetcherName = Fetcher(getByPrimaryKeySeq)")
    file.add()
  }

  def addPrimaryKeyArguments(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = if (model.pkFields.nonEmpty) {
    model.pkFields.foreach { f =>
      addArgument(config, model, f, file)
      addSeqArgument(config, model, f, file)
    }
    file.add()
  }

  def addIndexArguments(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val filtered = model.indexedFields.filterNot(model.pkFields.contains)
    filtered.foreach { f =>
      addArgument(config, model, f, file)
      addSeqArgument(config, model, f, file)
    }
    if (filtered.nonEmpty) { file.add() }
  }

  def addArgument(config: ExportConfiguration, model: ExportModel, field: ExportField, file: ScalaFile) = if (model.pkFields.nonEmpty) {
    val graphQlArgType = ExportFieldGraphQL.argType(config, field)
    file.add(s"""val ${model.propertyName}${field.className}Arg = Argument("${field.propertyName}", $graphQlArgType)""")
  }

  def addSeqArgument(config: ExportConfiguration, model: ExportModel, field: ExportField, file: ScalaFile) = {
    val graphQlSeqArgType = ExportFieldGraphQL.listArgType(config, field)
    val arg = s"""Argument("${field.propertyName}s", $graphQlSeqArgType)"""
    file.add(s"""val ${model.propertyName}${field.className}SeqArg = $arg""")
  }

  def addIndexedFields(model: ExportModel, file: ScalaFile) = {
    model.indexedFields.foreach { field =>
      val comma = if (model.indexedFields.lastOption.contains(field)) { "" } else { "," }
      val listType = s"ListType(${model.propertyName}Type)"
      val arg = s"${model.propertyName}${field.className}Arg"
      val argPull = if (field.required) {
        s"c.arg($arg)"
      } else {
        s"""c.arg($arg).getOrElse(throw new IllegalStateException("No [${field.propertyName}] provided"))"""
      }
      val seqArg = s"${model.propertyName}${field.className}SeqArg"

      if (field.unique) {
        val optType = s"OptionType(${model.propertyName}Type)"
        file.add(s"""unitField(name = "${model.propertyName}By${field.className}", desc = None, t = $optType, f = (c, td) => {""", 1)
        file.add(s"""c.ctx.${model.serviceReference}.getBy${field.className}(c.ctx.creds, $argPull)(td).map(_.headOption)""")
        file.add(s"""}, $arg),""", -1)
        file.add(s"""unitField(name = "${model.propertyPlural}By${field.className}Seq", desc = None, t = $listType, f = (c, td) => {""", 1)
        file.add(s"""c.ctx.${model.serviceReference}.getBy${field.className}Seq(c.ctx.creds, c.arg($seqArg))(td)""")
        file.add(s"""}, $seqArg)$comma""", -1)
      } else {
        file.add(s"""unitField(name = "${model.propertyPlural}By${field.className}", desc = None, t = $listType, f = (c, td) => {""", 1)
        file.add(s"""c.ctx.${model.serviceReference}.getBy${field.className}(c.ctx.creds, $argPull)(td)""")
        file.add(s"""}, $arg),""", -1)
        file.add(s"""unitField(name = "${model.propertyPlural}By${field.className}Seq", desc = None, t = $listType, f = (c, td) => {""", 1)
        file.add(s"""c.ctx.${model.serviceReference}.getBy${field.className}Seq(c.ctx.creds, c.arg($seqArg))(td)""")
        file.add(s"""}, $seqArg)$comma""", -1)
      }
    }
  }
}
