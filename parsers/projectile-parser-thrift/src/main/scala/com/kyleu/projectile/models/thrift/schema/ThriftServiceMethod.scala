package com.kyleu.projectile.models.thrift.schema

import com.facebook.swift.parser.model.{ThriftMethod, ThriftType}

import scala.jdk.CollectionConverters._

object ThriftServiceMethod {
  def fromThrift(f: ThriftMethod) = ThriftServiceMethod(f.getName, f.getArguments.asScala.toIndexedSeq.map(ThriftStructField.fromThrift), f.getReturnType)
}

case class ThriftServiceMethod(key: String, arguments: Seq[ThriftStructField], returnType: ThriftType)
