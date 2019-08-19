package com.kyleu.projectile.models.thrift.schema

import com.facebook.swift.parser.model.Union

import scala.jdk.CollectionConverters._

object ThriftUnion {
  def fromStruct(s: Union, pkg: Seq[String]) = {
    ThriftUnion(key = s.getName, pkg = pkg, types = s.getFields.asScala.toIndexedSeq.map(ThriftUnionMember.fromThrift))
  }
}

case class ThriftUnion(
    key: String,
    pkg: Seq[String],
    types: Seq[ThriftUnionMember]
) {
  override val toString = s"Union [${(pkg :+ key).mkString(".")}] with types [${types.map(f => f.key + ": " + f.t).mkString(", ")}]"
}

