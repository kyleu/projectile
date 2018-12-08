package com.projectile.models.graphql.parse

import com.projectile.models.export.ExportField
import com.projectile.models.export.typ.FieldType
import com.projectile.models.output.ExportHelper
import sangria.ast.{Selection, Type, Value}
import sangria.schema.Schema

object GraphQLTypeParser {
  def getField(schema: Schema[_, _], name: String, t: Type, idx: Int, defaultValue: Option[Value]) = {
    val (required, newT) = true -> FieldType.StringType
    ExportField(
      key = name,
      propertyName = ExportHelper.toIdentifier(name),
      title = ExportHelper.toDefaultTitle(name),
      description = None,
      idx = idx,
      t = newT,
      defaultValue = defaultValue.map(_.toString),
      required = required
    )
  }

  def fieldsForSelections(schema: Schema[_, _], selections: Seq[Selection]) = {
    Seq.empty[ExportField]
  }
}
