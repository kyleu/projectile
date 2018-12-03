package com.projectile.services.graphql.original

import com.projectile.services.graphql.original.GraphQLQueryParseService.ClassName
import sangria.ast.{ListType, NamedType, NotNullType, Type}

object GraphQLInputTranslations {
  def scalaType(typ: Type): String = typ match {
    case NotNullType(x, _) => scalaType(x) match {
      case t if t.startsWith("Option[") => t.stripPrefix("Option[").stripSuffix("]")
      case t => t
    }
    case ListType(x, _) => s"Seq[${scalaType(x)}]"
    case NamedType(name, _) => name match {
      case "FilterInput" => "Filter"
      case "OrderByInput" => "OrderBy"
      case _ => "Option[" + name + "]"
    }
  }

  def scalaImport(providedPrefix: Seq[String], t: Type, nameMap: Map[String, ClassName]): Option[(Seq[String], String)] = t match {
    case NotNullType(x, _) => scalaImport(providedPrefix, x, nameMap)
    case ListType(x, _) => scalaImport(providedPrefix, x, nameMap)
    case _ => t.namedType.renderCompact match {
      case "UUID" => Some(Seq("java", "util") -> "UUID")
      case "FilterInput" => Some((providedPrefix ++ Seq("models", "result", "filter")) -> "Filter")
      case "OrderByInput" => Some((providedPrefix ++ Seq("models", "result", "orderBy")) -> "OrderBy")
      case x => nameMap.get(x).map(x => x.pkg.toSeq -> x.cn)
    }
  }

  def defaultVal(typ: String) = typ match {
    case _ if typ.startsWith("Seq[") => " = Nil"
    case _ if typ.startsWith("Option[") => " = None"
    case _ => ""
  }
}
