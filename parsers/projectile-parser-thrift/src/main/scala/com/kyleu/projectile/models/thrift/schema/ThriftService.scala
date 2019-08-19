package com.kyleu.projectile.models.thrift.schema

import com.facebook.swift.parser.model.Service

import scala.jdk.CollectionConverters._

object ThriftService {
  def fromThrift(s: Service, pkg: Seq[String]) = {
    val methods = s.getMethods.asScala.toIndexedSeq.map(ThriftServiceMethod.fromThrift)
    ThriftService(key = s.getName, pkg = pkg, methods = methods)
  }
}

case class ThriftService(
    key: String,
    pkg: Seq[String],
    methods: Seq[ThriftServiceMethod]
) {
  override val toString = s"Service [${(pkg :+ key).mkString(".")}] with methods [${methods.mkString(", ")}]"
}
