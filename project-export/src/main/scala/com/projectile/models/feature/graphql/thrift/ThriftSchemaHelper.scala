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

  def mapsFor(t: String, req: Boolean = true): String = {
    t match {
      case _ if !req => if (mapsFor(t).nonEmpty) {
        s".map(some => some${mapsFor(t)})"
      } else {
        ""
      }
      case _ if t.startsWith("Seq[") => if (mapsFor(t.drop(4).dropRight(1)).nonEmpty) {
        s".map(el => el${mapsFor(t.drop(4).dropRight(1))})"
      } else {
        ""
      }
      case _ if t.startsWith("Set[") => if (mapsFor(t.drop(4).dropRight(1)).nonEmpty) {
        s".map(el => el${mapsFor(t.drop(4).dropRight(1))}).toSeq"
      } else {
        ".toSeq"
      }
      case x if x.startsWith("Map[") => s".toString"
      case _ => ""
    }
  }

  def getImportType(t: FieldType): Option[String] = t match {
    case _ if FieldType.scalars(t) => None
    case FieldType.MapType(_, v) => getImportType(v)
    case FieldType.ListType(typ) => getImportType(typ)
    case FieldType.SetType(typ) => getImportType(typ)
    case _ => Some(t.value)
  }
}
