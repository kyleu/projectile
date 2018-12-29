package com.kyleu.projectile.services.thrift

import com.facebook.swift.parser.model._
import com.kyleu.projectile.models.thrift.schema.{ThriftIntEnum, ThriftService, ThriftStringEnum, ThriftStruct}

case class ThriftParseResult(
    filename: String,
    reference: Boolean,
    pkg: Seq[String],
    decls: Seq[Definition],
    includes: Seq[ThriftParseResult],
    lines: Seq[String]
) {
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

  lazy val stringEnums = decls.filter(_.isInstanceOf[StringEnum]).map(_.asInstanceOf[StringEnum]).map(ThriftStringEnum.fromStringEnum(_, pkg))
  lazy val allStringEnums = stringEnums ++ includes.flatMap(_.stringEnums)

  lazy val intEnums = decls.filter(_.isInstanceOf[IntegerEnum]).map(_.asInstanceOf[IntegerEnum]).map(ThriftIntEnum.fromIntEnum(_, pkg))
  lazy val allIntEnums = intEnums ++ includes.flatMap(_.intEnums)

  lazy val structs = decls.filter(_.isInstanceOf[Struct]).map(_.asInstanceOf[Struct]).map(ThriftStruct.fromStruct(_, pkg))
  lazy val allStructs = structs ++ includes.flatMap(_.structs)

  lazy val services = decls.filter(_.isInstanceOf[Service]).map(_.asInstanceOf[Service]).map(ThriftService.fromThrift(_, pkg))
  lazy val allServices = services ++ includes.flatMap(_.services)
}
