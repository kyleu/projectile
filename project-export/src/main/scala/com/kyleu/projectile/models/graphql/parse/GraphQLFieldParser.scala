package com.kyleu.projectile.models.graphql.parse

import com.kyleu.projectile.models.export.ExportField
import com.kyleu.projectile.models.output.ExportHelper
import sangria.ast._
import sangria.schema.{InputType, OutputType, Schema}

object GraphQLFieldParser {
  def getField(ctx: String, schema: Schema[_, _], doc: Document, name: String, t: Type, defaultValue: Option[Value]) = {
    val (required, newT) = GraphQLTypeParser.getType(s"$ctx($name: ${t.renderCompact})", schema, doc, t)
    ExportField(
      key = name,
      propertyName = ExportHelper.toIdentifier(name),
      title = ExportHelper.toDefaultTitle(name),
      description = None,
      t = newT,
      defaultValue = defaultValue.map(_.toString),
      required = required
    )
  }

  def getInputField(ctx: String, schema: Schema[_, _], name: String, t: InputType[_]) = {
    val (required, newT) = GraphQLTypeParser.getInputType(s"$ctx($name: $t)", schema, t)
    ExportField(
      key = name,
      propertyName = ExportHelper.toIdentifier(name),
      title = ExportHelper.toDefaultTitle(name),
      description = None,
      t = newT,
      defaultValue = None,
      required = required
    )
  }

  def getOutputField(ctx: String, schema: Schema[_, _], doc: Document, name: String, t: OutputType[_], selections: Seq[Selection]) = {
    val (required, newT) = GraphQLTypeParser.getOutputType(s"$ctx($name: $t)", schema, doc, t, selections)
    ExportField(
      key = name,
      propertyName = ExportHelper.toIdentifier(name),
      title = ExportHelper.toDefaultTitle(name),
      description = None,
      t = newT,
      defaultValue = None,
      required = required
    )
  }
}
