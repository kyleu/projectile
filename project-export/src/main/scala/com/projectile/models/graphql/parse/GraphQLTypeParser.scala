package com.projectile.models.graphql.parse

import com.projectile.models.export.typ.FieldType
import sangria.ast._
import sangria.schema.{EnumType, InputObjectType, ScalarType, Schema}

object GraphQLTypeParser {
  def getType(ctx: String, schema: Schema[_, _], doc: Document, t: Type): (Boolean, FieldType) = t match {
    case NotNullType(ofType, _) => true -> getType(ctx, schema, doc, ofType)._2
    case ListType(ofType, _) => true -> FieldType.ListType(getType(ctx, schema, doc, ofType)._2)
    case named: NamedType => false -> getNamedType(ctx, schema, doc, named)
  }

  private[this] def getNamedType(ctx: String, schema: Schema[_, _], doc: Document, named: NamedType) = schema.allTypes.get(named.name).map {
    case e: EnumType[_] => FieldType.EnumType(e.name)
    case i: InputObjectType[_] => FieldType.StructType(i.name)
    case _: ScalarType[_] => FieldType.StringType // TODO
    case x => throw new IllegalStateException(s"Unhandled GraphQL type [$x] for type [$ctx]")
  }.orElse {
    doc.definitions.collectFirst {
      case x: InputObjectTypeDefinition if x.name == named.name => FieldType.StructType(x.name)
    }
  }.getOrElse {
    val tCand = schema.allTypes.keys.toSeq.sorted.mkString(", ")
    throw new IllegalStateException(s"Missing GraphQL type [${named.name}] for type [$ctx] among candidates [$tCand]")
  }
}
