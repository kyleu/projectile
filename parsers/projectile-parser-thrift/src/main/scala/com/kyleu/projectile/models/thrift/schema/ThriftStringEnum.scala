package com.kyleu.projectile.models.thrift.schema

import com.facebook.swift.parser.model.StringEnum

import scala.jdk.CollectionConverters._

object ThriftStringEnum {
  def fromStringEnum(e: StringEnum, pkg: Seq[String]) = ThriftStringEnum(e.getName, pkg, e.getValues.asScala.toIndexedSeq)
}

case class ThriftStringEnum(
    key: String,
    pkg: Seq[String],
    values: Seq[String]
) {
  override val toString = s"String enum [${(pkg :+ key).mkString(".")}] with values [${values.mkString(", ")}]"
}
