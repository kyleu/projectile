package com.projectile.services.thrift

import com.facebook.swift.parser.model._
import com.projectile.models.output.ExportHelper
import com.projectile.models.thrift.schema.{ThriftIntEnum, ThriftService, ThriftStringEnum, ThriftStruct}

object ThriftParseResult {
  case class Metadata(
      typedefs: Map[String, String],
      enums: Map[String, String],
      pkgMap: Map[String, Seq[String]]
  )
}

case class ThriftParseResult(
    filename: String,
    srcPkg: Seq[String],
    decls: Seq[Definition],
    includes: Seq[ThriftParseResult],
    lines: Seq[String]
) {
  lazy val tgtPkg = srcPkg.dropRight(1)
  lazy val pkgMap: Map[String, Seq[String]] = ((filename.stripSuffix(".thrift") -> tgtPkg) +: includes.flatMap(r => r.pkgMap.toSeq)).toMap

  lazy val comments = lines.filter(_.trim.startsWith("#")).map(_.trim.stripPrefix("#").trim)
  lazy val exportModelRoot = comments.find(_.startsWith("exportModelRoot")).map(_.stripPrefix("exportModelRoot").trim.stripPrefix("=").trim)

  lazy val typedefs = decls.filter(_.isInstanceOf[Typedef]).map(_.asInstanceOf[Typedef]).map { t =>
    t.getName -> (t.getType match {
      case i: IdentifierType => i.getName
      case b: BaseType => b.getType.toString
      case _ => throw new IllegalStateException("Cannot handle complex typedefs: " + t)
    })
  }.toMap
  lazy val allTypedefs = typedefs ++ includes.flatMap(_.typedefs)

  private[this] val pkg = srcPkg.dropRight(1)

  lazy val stringEnums = decls.filter(_.isInstanceOf[StringEnum]).map(_.asInstanceOf[StringEnum]).map(ThriftStringEnum.fromStringEnum(_, pkg))
  lazy val allStringEnums = stringEnums ++ includes.flatMap(_.stringEnums)
  lazy val stringEnumNames = stringEnums.map(_.key)
  lazy val stringEnumString = stringEnums.map(e => s"  ${e.key} (${e.values.size} values)").mkString("\n")

  lazy val intEnums = decls.filter(_.isInstanceOf[IntegerEnum]).map(_.asInstanceOf[IntegerEnum]).map(ThriftIntEnum.fromIntEnum(_, pkg))
  lazy val allIntEnums = intEnums ++ includes.flatMap(_.intEnums)
  lazy val intEnumNames = intEnums.map(_.key)
  lazy val intEnumString = intEnums.map(e => s"  ${e.key} (${e.values.size} values)").mkString("\n")

  lazy val enumDefaults = {
    val s = allStringEnums.map(e => e.key -> ExportHelper.toClassName(e.values.head))
    val i = allIntEnums.map(e => e.key -> ExportHelper.toClassName(e.values.head._1))
    (s ++ i).toMap
  }

  lazy val metadata = ThriftParseResult.Metadata(allTypedefs, enumDefaults, pkgMap)

  lazy val structs = decls.filter(_.isInstanceOf[Struct]).map(_.asInstanceOf[Struct]).map(ThriftStruct.fromStruct(_, pkg))
  lazy val allStructs = structs ++ includes.flatMap(_.structs)
  lazy val structNames = structs.map(_.key)
  lazy val structString = structs.map(struct => s"  ${struct.key} (${struct.fields.size} fields)").mkString("\n")

  lazy val services = decls.filter(_.isInstanceOf[Service]).map(_.asInstanceOf[Service]).map(ThriftService.fromThrift)
  lazy val allServices = services ++ includes.flatMap(_.services)
  lazy val serviceNames = services.map(_.key)
  lazy val serviceString = services.map(struct => s"  ${struct.key} (${struct.methods.size} methods)").mkString("\n")

  lazy val summaryString = s"""[[[$filename]]]
    |Package: [${srcPkg.mkString(".")}]
    |Models:
    |$structString
    |Services:
    |$serviceString
  """.stripMargin.trim

  override lazy val toString = {
    val incSummary = if (includes.isEmpty) { "" } else { includes.map(_.summaryString).mkString("\n\n") + "\n\n" }
    incSummary + summaryString
  }
}
