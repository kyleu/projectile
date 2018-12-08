package com.projectile.models.feature.graphql.db

import com.projectile.models.export.ExportModel
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.export.typ.FieldType.EnumType
import com.projectile.models.output.OutputPath
import com.projectile.models.output.file.ScalaFile

object SchemaFile {
  val resultArgs = "paging = r.paging, filters = r.args.filters, orderBys = r.args.orderBys, totalCount = r.count, results = r.results, durationMs = r.dur"

  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ScalaFile(path = OutputPath.ServerSource, config.applicationPackage ++ Seq("models") ++ model.pkg, model.className + "Schema")

    model.fields.foreach(_.t match {
      case EnumType(key) =>
        val e = config.getEnum(key)
        file.addImport(config.applicationPackage ++ e.modelPackage :+ e.className + "Schema", s"${e.propertyName}EnumType")
      case _ => // noop
    })

    if (model.pkColumns.nonEmpty && (!model.pkg.contains("note"))) {
      config.addCommonImport(file, "NoteSchema")
    }
    SchemaHelper.addImports(config, file)

    file.add(s"""object ${model.className}Schema extends GraphQLSchemaHelper("${model.propertyName}") {""", 1)
    SchemaHelper.addPrimaryKey(config, model, file)
    SchemaHelper.addPrimaryKeyArguments(config, model, file)
    SchemaHelper.addIndexArguments(config, model, file)
    SchemaForeignKey.writeSchema(config, model, file)
    addObjectType(config, model, file)
    addQueryFields(model, file)
    SchemaMutationHelper.addMutationFields(config, model, file)
    file.add()
    file.add(s"private[this] def toResult(r: GraphQLSchemaHelper.SearchResult[${model.className}]) = {", 1)
    file.add(s"${model.className}Result($resultArgs)")
    file.add("}", -1)
    file.add("}", -1)
    file
  }

  private[this] def addObjectType(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val columnsDescriptions = model.fields.flatMap(col => col.description.map(d => s"""DocumentField("${col.propertyName}", "$d")"""))
    val references = model.validReferences(config)
    if (columnsDescriptions.isEmpty && model.foreignKeys.isEmpty && references.isEmpty) {
      file.add(s"implicit lazy val ${model.propertyName}Type: sangria.schema.ObjectType[GraphQLContext, ${model.className}] = deriveObjectType()")
    } else {
      file.add(s"implicit lazy val ${model.propertyName}Type: sangria.schema.ObjectType[GraphQLContext, ${model.className}] = deriveObjectType(", 1)
      columnsDescriptions.foreach {
        case d if columnsDescriptions.lastOption.contains(d) && references.isEmpty => file.add(d)
        case d => file.add(d + ",")
      }
      if (model.pkColumns.nonEmpty || model.foreignKeys.nonEmpty || references.nonEmpty) {
        file.add("sangria.macros.derive.AddFields(", 1)
      }
      SchemaReferencesHelper.writeFields(config, model, file)
      SchemaForeignKey.writeFields(config, model, file)
      if (model.pkColumns.nonEmpty) {
        file.add("Field(", 1)
        file.add("""name = "relatedNotes",""")
        file.add("""fieldType = ListType(NoteSchema.noteType),""")
        val pkArgs = model.pkFields.map(f => "c.value." + f.propertyName).mkString(", ")
        file.add(s"""resolve = c => c.ctx.app.coreServices.notes.getFor(c.ctx.creds, "${model.propertyName}", $pkArgs)(c.ctx.trace)""")
        file.add(")", -1)
      }
      if (model.pkColumns.nonEmpty || model.foreignKeys.nonEmpty || references.nonEmpty) {
        file.add(")", -1)
      }
      file.add(")", -1)
    }
    file.add()
    file.add(s"implicit lazy val ${model.propertyName}ResultType: sangria.schema.ObjectType[GraphQLContext, ${model.className}Result] = deriveObjectType()")
    file.add()
  }

  private[this] def addQueryFields(model: ExportModel, file: ScalaFile) = {
    file.add("val queryFields = fields(", 1)

    if (model.pkFields.nonEmpty) {
      file.add(s"""unitField(name = "${model.propertyName}", desc = None, t = OptionType(${model.propertyName}Type), f = (c, td) => {""", 1)
      val args = model.pkFields.map(pkField => pkField -> s"${model.propertyName}${pkField.className}Arg")
      file.add(s"""c.ctx.${model.serviceReference}.getByPrimaryKey(c.ctx.creds, ${
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
        file.add(s"""c.ctx.${model.serviceReference}.getByPrimaryKeySeq(c.ctx.creds, c.arg($arg))(td)""")
        file.add(s"}, $arg),", -1)
      case _ => // noop
    }

    file.add(s"""unitField(name = "${model.propertyName}Search", desc = None, t = ${model.propertyName}ResultType, f = (c, td) => {""", 1)
    file.add(s"""runSearch(c.ctx.${model.serviceReference}, c, td).map(toResult)""")
    file.add(s"}, queryArg, reportFiltersArg, orderBysArg, limitArg, offsetArg)${if (model.indexedFields.nonEmpty) { "," } else { "" }}", -1)

    SchemaHelper.addIndexedFields(model, file)

    file.add(")", -1)
  }
}
