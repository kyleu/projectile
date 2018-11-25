package com.projectile.models.thrift.schema

import com.facebook.swift.parser.model.IntegerEnum

import scala.collection.JavaConverters._

object ThriftIntEnum {
  def fromIntEnum(e: IntegerEnum) = {
    ThriftIntEnum(e.getName, e.getFields.asScala.sortBy(_.getValue).map(x => x.getName -> x.getValue.toInt))
  }
}

case class ThriftIntEnum(
    key: String,
    values: Seq[(String, Int)]
)
