package com.projectile.models.graphql.parse

import com.projectile.models.export.ExportField
import sangria.ast._
import sangria.schema.Schema

object GraphQLSelectionParser {
  def fieldsForSelections(schema: Schema[_, _], doc: Document, selections: Seq[Selection]) = {
    val (spreads, fields) = distribute(selections)

    // TODO

    Seq.empty[ExportField]
  }

  private[this] def distribute(sels: Seq[Selection]) = {
    val spreads = sels.flatMap {
      case x: FragmentSpread => Some(x)
      case x: InlineFragment => Some(x)
      case _ => None
    }
    val fields = sels.flatMap {
      case x: Field => Some(x)
      case _ => None
    }
    spreads.toList -> fields.toList
  }
}
