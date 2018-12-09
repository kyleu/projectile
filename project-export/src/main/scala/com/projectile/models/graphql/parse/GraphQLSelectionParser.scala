package com.projectile.models.graphql.parse

import com.projectile.models.export.ExportField
import sangria.ast._
import sangria.schema.Schema

object GraphQLSelectionParser {
  def fieldsForSelections(schema: Schema[_, _], doc: Document, ctx: String, t: NamedType, selections: Seq[Selection]) = {
    val (spreads, inlines, fields) = distribute(selections)

    val spreadFields = spreads.flatMap(parseSpread)
    val inlineFields = inlines.flatMap(parseInline)
    val fieldFields = fields.flatMap(f => parseField(schema, doc, ctx, t, f))

    spreadFields ++ inlineFields ++ fieldFields
  }

  private[this] def distribute(sels: Seq[Selection]) = {
    val spreads = sels.collect { case x: FragmentSpread => x }
    val inlines = sels.collect { case x: InlineFragment => x }
    val fields = sels.collect { case x: Field => x }
    (spreads.toList, inlines.toList, fields.toList)
  }

  private[this] def parseSpread(spread: FragmentSpread): Seq[ExportField] = spread match {
    case _ => throw new IllegalStateException(spread.toString)
  }

  private[this] def parseInline(frag: InlineFragment): Seq[ExportField] = frag match {
    case _ => throw new IllegalStateException(frag.toString)
  }

  private[this] def parseField(schema: Schema[_, _], doc: Document, ctx: String, t: NamedType, field: Field): Option[ExportField] = {
    None
  }
}
