package com.kyleu.projectile.models.thrift.schema

import com.facebook.swift.parser.model.StringEnum

import scala.collection.JavaConverters._

object ThriftStringEnum {
  def fromStringEnum(e: StringEnum, pkg: Seq[String]) = ThriftStringEnum(e.getName, pkg, e.getValues.asScala)
}

case class ThriftStringEnum(
    key: String,
    pkg: Seq[String],
    values: Seq[String]
)
