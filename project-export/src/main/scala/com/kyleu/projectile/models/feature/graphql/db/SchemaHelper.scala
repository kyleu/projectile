package com.kyleu.projectile.models.feature.graphql.db

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
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
    file.addMarker("fetcher", (model.graphqlPackage(config) :+ (model.className + "Schema")).mkString(".") + "." + fetcherName)
    file.add(s"val $fetcherName = Fetcher(getByPrimaryKeySeq)")
    file.add()
  }

  def addPrimaryKeyArguments(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = if (model.pkFields.nonEmpty) {
    model.pkFields.foreach(f => addArguments(config, model, f, file))
    file.add()
  }

  def addSearchArguments(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    model.extraFields.foreach { f =>
      addArguments(config, model, f, file)
    }
    if (model.extraFields.nonEmpty) { file.add() }
  }

  def addArguments(config: ExportConfiguration, model: ExportModel, field: ExportField, file: ScalaFile) = if (model.pkFields.nonEmpty) {
    val graphQlArgType = ExportFieldGraphQL.argType(config, field.className, field.t, field.required)
    file.add(s"""val ${model.propertyName}${field.className}Arg = Argument("${field.propertyName}", $graphQlArgType)""")
    val seqGraphQlArgType = ExportFieldGraphQL.argType(config, field.className, FieldType.ListType(field.t), field.required)
    file.add(s"""val ${model.propertyName}${field.className}SeqArg = Argument("${field.propertyName}s", $seqGraphQlArgType)""")
  }

  def addSearchFields(model: ExportModel, file: ScalaFile) = model.extraFields.foreach { field =>
    val comma = if (model.extraFields.lastOption.contains(field)) { "" } else { "," }
    val listType = s"ListType(${model.propertyName}Type)"
    val arg = s"${model.propertyName}${field.className}Arg"

    val argSuffix = field.t match {
      case FieldType.ListType(_) => ".toList"
      case FieldType.SetType(_) => ".toSet"
      case FieldType.TagsType => """.map(_ => Tag("?", "?"))"""
      case _ => ""
    }
    val argPull = s"c.arg($arg)$argSuffix"

    val seqArgSuffix = field.t match {
      case FieldType.ListType(_) => ".map(_.toList)"
      case FieldType.SetType(_) => ".map(_.toSet)"
      case FieldType.TagsType => ".map(_ => Nil)"
      case _ => ""
    }
    val seqArgName = s"${model.propertyName}${field.className}SeqArg"
    val seqArg = s"c.arg($seqArgName)$seqArgSuffix"

    if (field.unique) {
      val optType = s"OptionType(${model.propertyName}Type)"
      file.add(s"""unitField(name = "${model.propertyName}By${field.className}", desc = None, t = $optType, f = (c, td) => {""", 1)
      file.add(s"""c.ctx.${model.serviceReference}.getBy${field.className}(c.ctx.creds, $argPull)(td).map(_.headOption)""")
      file.add(s"""}, $arg),""", -1)
      file.add(s"""unitField(name = "${model.propertyPlural}By${field.className}Seq", desc = None, t = $listType, f = (c, td) => {""", 1)
      file.add(s"""c.ctx.${model.serviceReference}.getBy${field.className}Seq(c.ctx.creds, $seqArg)(td)""")
      file.add(s"""}, $seqArgName)$comma""", -1)
    } else {
      file.add(s"""unitField(name = "${model.propertyPlural}By${field.className}", desc = None, t = $listType, f = (c, td) => {""", 1)
      file.add(s"""c.ctx.${model.serviceReference}.getBy${field.className}(c.ctx.creds, $argPull)(td)""")
      file.add(s"""}, $arg),""", -1)
      file.add(s"""unitField(name = "${model.propertyPlural}By${field.className}Seq", desc = None, t = $listType, f = (c, td) => {""", 1)
      file.add(s"""c.ctx.${model.serviceReference}.getBy${field.className}Seq(c.ctx.creds, $seqArg)(td)""")
      file.add(s"""}, $seqArgName)$comma""", -1)
    }
  }
}
