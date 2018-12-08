package com.projectile.models.graphql.parse

import com.projectile.models.export.typ.FieldType
import sangria.ast._
import sangria.schema.{EnumType, InputObjectType, InputType, ListInputType, OptionInputType, ScalarAlias, ScalarType, Schema}

object GraphQLTypeParser {
  def getType(ctx: String, schema: Schema[_, _], doc: Document, t: Type): (Boolean, FieldType) = t match {
    case NotNullType(ofType, _) => true -> getType(ctx, schema, doc, ofType)._2
    case ListType(ofType, _) => true -> FieldType.ListType(getType(ctx, schema, doc, ofType)._2)
    case named: NamedType => false -> schema.allTypes.get(named.name).map {
      case e: EnumType[_] => FieldType.EnumType(e.name)
      case i: InputObjectType[_] => FieldType.StructType(i.name)
      case s: ScalarType[_] => getScalarType(s.name)
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

  def getInputType(ctx: String, schema: Schema[_, _], t: InputType[_]): (Boolean, FieldType) = t match {
    case e: EnumType[_] => true -> FieldType.EnumType(e.name)
    case i: InputObjectType[_] => true -> FieldType.StructType(i.name)
    case l: ListInputType[_] => true -> FieldType.ListType(getInputType(ctx, schema, l.ofType)._2)
    case o: OptionInputType[_] => false -> getInputType(ctx, schema, o.ofType)._2
    case s: ScalarAlias[_, _] => true -> getScalarType(s.aliasFor.name)
    case s: ScalarType[_] => true -> getScalarType(s.name)
  }

  private[this] def getScalarType(name: String) = name match {
    case "Boolean" => FieldType.BooleanType
    case "BigDecimal" => FieldType.BigDecimalType
    case "Date" => FieldType.DateType
    case "DateTime" => FieldType.TimestampType
    case "Int" => FieldType.IntegerType
    case "Float" => FieldType.DoubleType
    case "Long" => FieldType.LongType
    case "Time" => FieldType.TimeType
    case "String" => FieldType.StringType
    case "UUID" => FieldType.UuidType
    case "Var" => FieldType.StringType
    case _ => throw new IllegalStateException(s"Unhandled scalar type [$name]")
  }
}
