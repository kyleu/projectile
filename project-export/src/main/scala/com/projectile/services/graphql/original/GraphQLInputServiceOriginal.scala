package com.projectile.services.graphql.original

import com.projectile.models.output.OutputPath
import com.projectile.models.output.file.ScalaFile
import sangria.ast._
import sangria.schema.Schema

object GraphQLInputServiceOriginal {
  import GraphQLQueryParseService._

  def inputFile(cfg: GraphQLExportConfig, d: InputObjectTypeDefinition, nameMap: Map[String, ClassName], schema: Schema[_, _], enums: Seq[String]) = {
    if (nameMap.get(d.name).exists(_.provided)) {
      None
    } else {
      val cn = nameMap(d.name)
      val file = ScalaFile(OutputPath.ServerSource, cn.pkg, cn.cn)
      file.addImport(cfg.providedPrefix ++ Seq("util", "JsonSerializers"), "_")

      meaningfulComments(d.comments).foreach(c => file.add("// " + c))

      file.add(s"object ${cn.cn} {", 1)
      file.add(s"implicit val jsonDecoder: Decoder[${cn.cn}] = deriveDecoder")
      file.add(s"implicit val jsonEncoder: Encoder[${cn.cn}] = deriveEncoder")
      file.add("}", -1)
      file.add()
      file.add(s"case class ${cn.cn}(", 2)
      addInputFields(cfg, file, cn.pkg, d.fields, nameMap, enums)
      file.add(")", -2)

      Some(file)
    }
  }

  private[this] def typeForSelections(file: ScalaFile, typ: Type, nameMap: Map[String, ClassName], enums: Seq[String]): String = {
    typ match {
      case ListType(t, _) => s"Seq[${typeForSelections(file, t, nameMap, enums)}]"
      case NotNullType(t, _) => typeForSelections(file, t, nameMap, enums) match {
        case x if x.startsWith("Option") => x.stripPrefix("Option[").stripSuffix("]")
        case x => x
      }
      case NamedType(n, _) => s"Option[" + (n match {
        case _ if enums.contains(n) =>
          val ecn = nameMap(n)
          file.addImport(ecn.pkg, ecn.cn)
          ecn.cn
        case "UUID" =>
          file.addImport(Seq("java", "util"), "UUID")
          "UUID"
        case "DateTime" =>
          file.addImport(Seq("java", "time"), "ZonedDateTime")
          "ZonedDateTime"
        case x => x
      }) + "]"
      case x => throw new IllegalStateException(s"Unhandled input type [$x].")
    }
  }

  private[this] def addInputFields(
    cfg: GraphQLExportConfig, file: ScalaFile, pkg: Array[String], fields: Vector[InputValueDefinition], nameMap: Map[String, ClassName], enums: Seq[String]
  ) = {
    fields.foreach { f =>
      val t = typeForSelections(file, f.valueType, nameMap, enums)
      val param = s"${f.name}: $t"
      val comma = if (fields.lastOption.contains(f)) { "" } else { "," }
      file.add(param + comma)
    }
  }
}
