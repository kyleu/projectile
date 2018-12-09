package com.projectile.models.graphql.parse

import com.projectile.models.export.ExportField
import sangria.ast._
import sangria.schema.{ObjectType, Schema, Type => Typ}

object GraphQLSelectionParser {
  def fieldsForSelections(ctx: String, schema: Schema[_, _], doc: Document, typ: Typ, selections: Seq[Selection]) = {
    val (spreads, inlines, fields) = distribute(selections)

    val spreadFields = spreads.flatMap(parseSpread)
    val inlineFields = inlines.flatMap(parseInline)
    val fieldFields = fields.flatMap(f => parseField(ctx, schema, doc, typ, f))

    spreadFields ++ inlineFields ++ fieldFields
  }

  private[this] def distribute(sels: Seq[Selection]) = {
    val spreads = sels.collect { case x: FragmentSpread => x }
    val inlines = sels.collect { case x: InlineFragment => x }
    val fields = sels.collect { case x: Field => x }
    (spreads.toList, inlines.toList, fields.toList)
  }

  private[this] def parseSpread(spread: FragmentSpread): Seq[ExportField] = spread match {
    case _ => Nil
  }

  private[this] def parseInline(frag: InlineFragment): Seq[ExportField] = frag match {
    case _ => throw new IllegalStateException(frag.toString)
  }

  private[this] def parseField(ctx: String, schema: Schema[_, _], doc: Document, typ: Typ, field: Field): Option[ExportField] = {
    val fieldType = typ match {
      case o: ObjectType[_, _] => o.fields.find(_.name == field.name) match {
        case Some(f) => f.fieldType
        case None =>
          val candidates = o.fields.map(_.name).sorted.mkString(", ")
          throw new IllegalStateException(s"Cannot find field [${field.name}] on type [${typ.namedType.name}] from [$candidates].")
      }
      case _ => throw new IllegalStateException(s"Cannot extract field type from [$typ]")
    }
    Some(GraphQLFieldParser.getOutputField(ctx, schema, field.name, fieldType, field.selections))
  }
}
