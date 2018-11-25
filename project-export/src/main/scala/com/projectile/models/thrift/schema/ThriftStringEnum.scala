package com.projectile.models.thrift.schema

import com.facebook.swift.parser.model.StringEnum

import scala.collection.JavaConverters._

object ThriftStringEnum {
  def fromStringEnum(e: StringEnum) = ThriftStringEnum(e.getName, e.getValues.asScala)
}

case class ThriftStringEnum(
    key: String,
    values: Seq[String]
)
