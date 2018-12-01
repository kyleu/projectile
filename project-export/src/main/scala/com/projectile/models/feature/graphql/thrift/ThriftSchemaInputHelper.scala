package com.projectile.models.feature.graphql.thrift

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.ExportHelper
import com.projectile.models.output.file.ScalaFile
import com.projectile.models.thrift.parse.ThriftFieldHelper

object ThriftSchemaInputHelper {
  /*
  def graphQlInputTypeFor(src: Option[(Seq[String], ScalaFile)], t: String, enums: Map[String, String], req: Boolean = true): String = t match {
    case _ if !req => s"OptionInputType(${graphQlInputTypeFor(src, t, enums)})"
    case "Long" => "LongType"
    case "Double" => "FloatType"
    case "Float" => "FloatType"
    case "Int" => "IntType"
    case "String" => "StringType"
    case "Boolean" => "BooleanType"
    case "Unit" => "BooleanType"
    case x if x.startsWith("Seq[") => s"ListInputType(${graphQlInputTypeFor(src, t.drop(4).dropRight(1), enums)})"
    case x if x.startsWith("Set[") => s"ListInputType(${graphQlInputTypeFor(src, t.drop(4).dropRight(1), enums)})"
    case x if x.startsWith("Map[") => s"StringType"
    case x if enums.contains(x) =>
      src.foreach(s => s._2.addImport(s._1 ++ Seq("graphql", s"${x}Schema"), s"${ExportHelper.toIdentifier(x)}Type"))
      ExportHelper.toIdentifier(x) + "Type"
    case x => ExportHelper.toIdentifier(x) + "InputType"
  }
  */

  /*
  def mapsFor(t: String, req: Boolean = true): String = t match {
    case _ if !req => s".map(some => some${mapsFor(t)})"
    case _ if t.startsWith("Seq[") => s".map(el => el${mapsFor(t.drop(4).dropRight(1))})"
    case _ if t.startsWith("Set[") => s".map(el => el${mapsFor(t.drop(4).dropRight(1))}).toSet"
    case x if x.startsWith("Map[") => s".toString"
    case _ => ""
  }
  */

  def getImportType(t: String): Option[String] = t match {
    case "Unit" | "Boolean" | "String" | "Int" | "Long" | "Double" => None
    case x if x.startsWith("Map[") => getImportType(ThriftFieldHelper.mapKeyValFor(x)._2)
    case x if x.startsWith("Seq[") => getImportType(t.drop(4).dropRight(1))
    case x if x.startsWith("Set[") => getImportType(t.drop(4).dropRight(1))
    case x => Some(x)
  }

  def addInputImports(pkg: Seq[String], types: Seq[String], config: ExportConfiguration, file: ScalaFile) = {
    types.foreach { colType =>
      getImportType(colType).foreach { impType =>
        config.enums.find(_.key == impType) match {
          case Some(e) => file.addImport(e.pkg :+ "models" :+ "graphql" :+ s"${impType}Schema", s"${ExportHelper.toIdentifier(impType)}Type")
          case None =>
        }
      }
    }
  }
}
