package com.projectile.models.feature.graphql.thrift

import com.facebook.swift.parser.model.ThriftType
import com.projectile.models.output.ExportHelper
import com.projectile.models.thrift.input.ThriftFileHelper
import com.projectile.models.thrift.parse.ThriftFieldHelper
import com.projectile.services.thrift.ThriftParseResult

object ThriftSchemaHelper {
  def graphQlTypeFor(t: String, req: Boolean = true): String = t match {
    case _ if !req => s"OptionType(${graphQlTypeFor(t)})"
    case "Long" => "LongType"
    case "Double" => "FloatType"
    case "Float" => "FloatType"
    case "Int" => "IntType"
    case "String" => "StringType"
    case "Boolean" => "BooleanType"
    case "Unit" => "BooleanType"
    case x if x.startsWith("Seq[") => s"ListType(${graphQlTypeFor(t.drop(4).dropRight(1))})"
    case x if x.startsWith("Set[") => s"ListType(${graphQlTypeFor(t.drop(4).dropRight(1))})"
    case x if x.startsWith("Map[") => s"StringType"
    case x => ExportHelper.toIdentifier(x) + "Type"
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

  case class ReplacedField(name: String, t: String, pkg: Seq[String], req: Boolean = true) {
    lazy val fullFieldDecl = s"""ReplaceField("$name", Field("$name", ${graphQlTypeFor(t, req)}, resolve = _.value.$name${mapsFor(t, req)}))"""
  }

  def getReplaceFields(pkg: Seq[String], types: Seq[(String, Boolean, ThriftType)], metadata: ThriftParseResult.Metadata): Seq[ReplacedField] = types.flatMap {
    case (n, r, t) => ThriftFileHelper.columnTypeFor(t, metadata) match {
      case (typ, x) if x.contains("Set[") || x.contains("Seq[") || x.contains("Map[") => if (mapsFor(x, r).nonEmpty) {
        Some(ReplacedField(name = n, t = typ.asScala, pkg = pkg, req = r))
      } else { None }
      case _ => None
    }
  }

  def getImportType(t: String): Option[String] = t match {
    case "Unit" | "Boolean" | "String" | "Int" | "Long" | "Double" => None
    case x if x.startsWith("Map[") => getImportType(ThriftFieldHelper.mapKeyValFor(x)._2)
    case x if x.startsWith("Seq[") => getImportType(t.drop(4).dropRight(1))
    case x if x.startsWith("Set[") => getImportType(t.drop(4).dropRight(1))
    case x => Some(x)
  }
}
