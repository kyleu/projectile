package com.projectile.models.feature.graphql.thrift

import com.projectile.models.export.typ.FieldType
import com.projectile.models.output.ExportHelper

object ThriftSchemaHelper {
  def graphQlTypeFor(t: FieldType, req: Boolean = true): String = t match {
    case _ if !req => s"OptionType(${graphQlTypeFor(t)})"
    case FieldType.LongType => "LongType"
    case FieldType.DoubleType => "FloatType"
    case FieldType.FloatType => "FloatType"
    case FieldType.IntegerType => "IntType"
    case FieldType.StringType => "StringType"
    case FieldType.BooleanType => "BooleanType"
    case FieldType.UnitType => "UnitType"
    case FieldType.ListType(typ) => s"ListType(${graphQlTypeFor(typ)})"
    case FieldType.SetType(typ) => s"ListType(${graphQlTypeFor(typ)})"
    case FieldType.MapType(_, _) => s"StringType"
    case x => ExportHelper.toIdentifier(x.value) + "Type"
  }
}
