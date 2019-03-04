package com.kyleu.projectile.models.graphql.parse

import com.kyleu.projectile.models.export.typ.FieldType
import sangria.ast._
import sangria.schema.{
  EnumType,
  InputObjectType,
  InputType,
  InterfaceType,
  ListInputType,
  ObjectType,
  OptionInputType,
  OptionType,
  OutputType,
  ScalarAlias,
  ScalarType,
  Schema,
  UnionType
}

object GraphQLTypeParser {
  def getType(ctx: String, schema: Schema[_, _], doc: Document, t: Type): (Boolean, FieldType) = t match {
    case NotNullType(ofType, _) => true -> getType(ctx, schema, doc, ofType)._2
    case ListType(ofType, _) => true -> FieldType.ListType(getType(ctx, schema, doc, ofType)._2)
    case named: NamedType if named.name == "Number" => false -> getScalarType("Int")
    case named: NamedType => false -> schema.allTypes.get(named.name).map {
      case e: EnumType[_] => FieldType.EnumType(e.name)
      case i: InputObjectType[_] => FieldType.StructType(i.name)
      case u: UnionType[_] => FieldType.UnionType(u.name, u.types.map(t => getOutputType(ctx, schema, doc, t, Nil)).map(_._2))
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

  def getOutputType(ctx: String, schema: Schema[_, _], doc: Document, t: OutputType[_], selections: Seq[Selection]): (Boolean, FieldType) = t match {
    case o: OptionType[_] => false -> getOutputType(ctx + "." + o.ofType, schema, doc, o.ofType, selections)._2
    case l: sangria.schema.ListType[_] => true -> FieldType.ListType(getOutputType(ctx + "." + l.ofType, schema, doc, l.ofType, selections)._2)

    case o: ObjectType[_, _] => GraphQLSelectionParser.fieldsForSelections(ctx, schema, doc, o, selections) match {
      case Left(name) => true -> FieldType.StructType(key = name)
      case Right(fields) => true -> FieldType.ObjectType(
        key = o.name + "Wrapper", fields = fields.map(f => com.kyleu.projectile.models.export.typ.ObjectField(k = f.key, t = f.t, req = f.required))
      )
    }

    case _: InterfaceType[_, _] => true -> FieldType.JsonType
    case u: UnionType[_] => true -> FieldType.UnionType(u.name, u.types.map(t => getOutputType(ctx, schema, doc, t, Nil)).map(_._2))

    case e: EnumType[_] => true -> FieldType.EnumType(e.name)
    case s: ScalarAlias[_, _] => true -> getScalarType(s.aliasFor.name)
    case s: ScalarType[_] => true -> getScalarType(s.name)
  }

  def getScalarType(name: String) = name match {
    case "Boolean" => FieldType.BooleanType
    case "BigDecimal" => FieldType.BigDecimalType
    case "Date" => FieldType.DateType
    case "DateTime" => FieldType.TimestampZonedType
    case "ID" => FieldType.StringType
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
