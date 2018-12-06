package com.projectile.models.feature.graphql.thrift

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.export.typ.FieldType
import com.projectile.models.output.ExportHelper

object ThriftSchemaHelper {
  def graphQlTypeFor(t: FieldType, config: ExportConfiguration, req: Boolean = true): String = t match {
    case _ if !req => s"OptionType(${graphQlTypeFor(t, config)})"
    case FieldType.LongType => "LongType"
    case FieldType.DoubleType => "FloatType"
    case FieldType.FloatType => "FloatType"
    case FieldType.IntegerType => "IntType"
    case FieldType.StringType => "StringType"
    case FieldType.BooleanType => "BooleanType"
    case FieldType.UnitType => "StringType"
    case FieldType.ListType(typ) => s"ListType(${graphQlTypeFor(typ, config)})"
    case FieldType.SetType(typ) => s"ListType(${graphQlTypeFor(typ, config)})"
    case FieldType.MapType(_, _) => s"StringType"
    case FieldType.EnumType(key) => config.getEnum(key).propertyName + "Type"
    case FieldType.StructType(key) => config.getModel(key).propertyName + "Type"
    case x => ExportHelper.toIdentifier(x.value) + "Type"
  }
}
