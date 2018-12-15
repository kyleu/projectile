package com.kyleu.projectile.models.thrift.schema

import com.facebook.swift.parser.model.IntegerEnum

import scala.collection.JavaConverters._

object ThriftIntEnum {
  def fromIntEnum(e: IntegerEnum, pkg: Seq[String]) = {
    ThriftIntEnum(e.getName, pkg, e.getFields.asScala.sortBy(_.getValue).map(x => x.getName -> x.getValue.toInt))
  }
}

case class ThriftIntEnum(
    key: String,
    pkg: Seq[String],
    values: Seq[(String, Int)]
)
