package com.kyleu.projectile.models.graphql.parse

import com.kyleu.projectile.models.export.ExportField
import sangria.ast._
import sangria.schema.{ObjectType, Schema, Type => Typ}

object GraphQLSelectionParser {
  def fieldsForSelections(ctx: String, schema: Schema[_, _], doc: Document, typ: Typ, selections: Seq[Selection]) = {
    def parseSpread(spread: FragmentSpread): String = {
      doc.fragments.get(spread.name).map(_.name).orElse {
        doc.definitions.collectFirst {
          case f: FragmentDefinition if f.name == spread.name => f.name
          case o: ObjectTypeDefinition if o.name == spread.name => o.name
          case i: InputObjectTypeDefinition if i.name == spread.name => i.name
        }
      }.orElse {
        schema.allTypes.get(spread.name).map(_.name)
      }.getOrElse {
        val definitionTypes = doc.definitions.map(defName).sorted.mkString(", ")
        val schemaTypes = schema.allTypes.keys.toSeq.sorted.mkString(", ")
        val fragmentTypes = doc.fragments.keys.toSeq.sorted.mkString(", ")
        val typeString = s"\n  Fragments: [$fragmentTypes]\n  Definitions: [$definitionTypes]\n  Schema Types: [$schemaTypes]"
        throw new IllegalStateException(s"Cannot load spread [${spread.name}] from$typeString for [$ctx]")
      }
    }

    def parseField(field: Field, doc: Document): ExportField = {
      val fieldType = typ match {
        case o: ObjectType[_, _] => o.fields.find(_.name == field.name) match {
          case Some(f) => f.fieldType
          case None =>
            val candidates = o.fields.map(_.name).sorted.mkString(", ")
            throw new IllegalStateException(s"Cannot find field [${field.name}] on type [${typ.namedType.name}] from [$candidates] for [$ctx:${field.name}]")
        }
        case _ => throw new IllegalStateException(s"Cannot extract field type from [$typ] for [$ctx:${field.name}]")
      }
      GraphQLFieldParser.getOutputField(ctx, schema, doc, field.name, fieldType, field.selections)
    }

    val (spreads, inlines, fields) = distribute(selections)

    if (inlines.nonEmpty) {
      throw new IllegalStateException(s"Cannot currently support inline fragments for [$ctx]")
    }

    spreads match {
      case h :: Nil => fields match {
        case Nil => Left(parseSpread(h))
        case _ => throw new IllegalStateException(s"Cannot currently support mixed fields and spreads for [$ctx]")
      }
      case Nil => Right(fields.map(parseField(_, doc)))
      case _ => throw new IllegalStateException(s"Cannot currently support multiple field spreads for [$ctx]")
    }
  }

  private[this] def distribute(sels: Seq[Selection]) = {
    val spreads = sels.collect { case x: FragmentSpread => x }
    val inlines = sels.collect { case x: InlineFragment => x }
    val fields = sels.collect { case x: Field => x }

    val others = sels.flatMap {
      case _: FragmentSpread => None
      case _: InlineFragment => None
      case _: Field => None
      case x => Some(x.getClass.getSimpleName)
    }.distinct

    if (others.nonEmpty) {
      throw new IllegalStateException(s"Unhandled selections [${others.mkString(", ")}]")
    }

    (spreads.toList, inlines.toList, fields.toList)
  }

  private[this] def defName(d: Definition) = d match {
    case f: FragmentDefinition => f.name
    case i: InputObjectTypeDefinition => i.name
    case o: ObjectTypeDefinition => o.name
    case o: OperationDefinition => o.name.getOrElse("DefaultOperation")
    case x => throw new IllegalStateException(s"Unhandled definition type [$x]")
  }
}
