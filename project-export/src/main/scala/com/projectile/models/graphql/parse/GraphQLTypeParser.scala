package com.projectile.models.graphql.parse

import com.projectile.models.export.typ.FieldType
import sangria.ast.{ListType, NamedType, NotNullType, Type}
import sangria.schema.Schema

object GraphQLTypeParser {
  def getType(schema: Schema[_, _], t: Type): (Boolean, FieldType) = t match {
    case NotNullType(ofType, _) => true -> getType(schema, ofType)._2
    case ListType(ofType, _) => true -> FieldType.ListType(getType(schema, ofType)._2)
    case named: NamedType => false -> getNamedType(schema, named)
  }

  private[this] def getNamedType(schema: Schema[_, _], named: NamedType) = {
    FieldType.StringType
  }
}
