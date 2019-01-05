package com.kyleu.projectile.models.feature.graphql.db

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.output.file.ScalaFile

object SchemaObjectType {
  def addObjectType(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val columnsDescriptions = model.fields.flatMap(col => col.description.map(d => s"""DocumentField("${col.propertyName}", "$d")"""))
    val references = model.validReferences(config)
    val withNotes = model.pkColumns.nonEmpty && model.features(ModelFeature.Notes)
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
      SchemaReferencesHelper.writeFields(config, model, file, model.foreignKeys.isEmpty && (!withNotes))
      SchemaForeignKey.writeFields(config, model, file, !withNotes)
      if (withNotes) {
        file.add("Field(", 1)
        file.add("""name = "relatedNotes",""")
        file.add("""fieldType = ListType(NoteSchema.noteType),""")
        val pkArgs = model.pkFields.map(f => "c.value." + f.propertyName).mkString(", ")
        file.add(s"""resolve = c => c.ctx.noteLookup(c.ctx.creds, "${model.propertyName}", $pkArgs)(c.ctx.trace)""")
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
}
