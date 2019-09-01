package com.kyleu.projectile.models.feature.graphql.db

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.export.typ.FieldType.EnumType
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.feature.graphql.db.SchemaHelper.injectedService
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile

object SchemaFile {
  val resultArgs = "paging = r.paging, filters = r.args.filters, orderBys = r.args.orderBys, totalCount = r.count, results = r.results, durationMs = r.dur"

  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ScalaFile(path = OutputPath.ServerSource, model.graphqlPackage(config), model.className + "Schema")

    file.addImport(model.modelPackage(config), model.className)
    file.addImport(model.modelPackage(config), model.className + "Result")

    model.fields.foreach { f =>
      f.t match {
        case EnumType(key) =>
          val e = config.getEnum(key, "schema file")
          file.addImport(e.graphqlPackage(config) :+ e.className + "Schema", s"${e.propertyName}EnumType")
        case _ => // noop
      }
    }

    if (model.pkColumns.nonEmpty && model.features(ModelFeature.Notes)) {
      // config.addCommonImport(file, "NoteSchema")
    }
    SchemaHelper.addImports(config, file)

    file.add(s"""object ${model.className}Schema extends GraphQLSchemaHelper("${model.propertyName}") {""", 1)
    SchemaHelper.addPrimaryKey(config, model, file)
    SchemaHelper.addPrimaryKeyArguments(config, model, file)
    SchemaHelper.addSearchArguments(config, model, file)
    SchemaForeignKey.writeSchema(config, model, file)
    SchemaObjectType.addObjectType(config, model, file)
    addQueryFields(config, model, file)
    SchemaMutationHelper.addMutationFields(config, model, file)
    file.add()
    file.add(s"private[this] def toResult(r: GraphQLSchemaHelper.SearchResult[${model.className}]) = {", 1)
    file.add(s"${model.className}Result($resultArgs)")
    file.add("}", -1)
    file.add("}", -1)
    file
  }

  private[this] def addQueryFields(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    file.add("val queryFields = fields(", 1)

    if (model.pkFields.nonEmpty) {
      file.add(s"""unitField(name = "${model.propertyName}", desc = None, t = OptionType(${model.propertyName}Type), f = (c, td) => {""", 1)
      val args = model.pkFields.map(pkField => pkField -> s"${model.propertyName}${pkField.className}Arg")
      file.add(s"""c.ctx.${injectedService(model, config)}.getByPrimaryKey(c.ctx.creds, ${
        args.map {
          case a if a._1.required => s"c.arg(${a._2})"
          case a => s"""c.arg(${a._2}).getOrElse(throw new IllegalStateException("No [${a._1.propertyName}] provided"))"""
        }.mkString(", ")
      })(td)""")
      file.add(s"}, ${args.map(_._2).mkString(", ")}),", -1)
    }

    model.pkFields match {
      case pkField :: Nil =>
        file.add(s"""unitField(name = "${model.propertyName}Seq", desc = None, t = ListType(${model.propertyName}Type), f = (c, td) => {""", 1)
        val arg = s"${model.propertyName}${pkField.className}SeqArg"
        file.add(s"""c.ctx.${injectedService(model, config)}.getByPrimaryKeySeq(c.ctx.creds, c.arg($arg))(td)""")
        file.add(s"}, $arg),", -1)
      case _ => // noop
    }

    file.add(s"""unitField(name = "${model.propertyName}Search", desc = None, t = ${model.propertyName}ResultType, f = (c, td) => {""", 1)
    file.add(s"""runSearch(c.ctx.${injectedService(model, config)}, c, td).map(toResult)""")
    file.add(s"}, queryArg, reportFiltersArg, orderBysArg, limitArg, offsetArg)${if (extraFields(model).nonEmpty) { "," } else { "" }}", -1)

    SchemaHelper.addSearchFields(config, model, file)

    file.add(")", -1)
  }

  def extraFields(model: ExportModel) = model.searchFields.filterNot(model.pkFields.contains).filter {
    case x if x.t == FieldType.TagsType => false
    case _ => true
  }
}
