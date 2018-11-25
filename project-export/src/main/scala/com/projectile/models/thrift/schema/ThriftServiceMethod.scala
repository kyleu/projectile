package com.projectile.models.thrift.schema

import com.facebook.swift.parser.model.ThriftMethod

import scala.collection.JavaConverters._

object ThriftServiceMethod {
  def fromThrift(f: ThriftMethod) = ThriftServiceMethod(f.getName, f.getArguments.asScala.map(ThriftStructField.fromThrift), f.getReturnType.toString)
}

case class ThriftServiceMethod(key: String, arguments: Seq[ThriftStructField], returnType: String)
