package com.kyleu.projectile.models.thrift.schema

import com.facebook.swift.parser.model.Struct

import scala.jdk.CollectionConverters._

object ThriftStruct {
  def fromStruct(s: Struct, pkg: Seq[String]) = {
    ThriftStruct(key = s.getName, pkg = pkg, fields = s.getFields.asScala.toIndexedSeq.map(ThriftStructField.fromThrift))
  }
}

final case class ThriftStruct(
    key: String,
    pkg: Seq[String],
    fields: Seq[ThriftStructField]
) {
  override val toString = s"Struct [${(pkg :+ key).mkString(".")}] with fields [${fields.map(f => f.key + ": " + f.t).mkString(", ")}]"
}

