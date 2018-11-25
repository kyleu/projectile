package com.projectile.models.thrift.schema

import com.facebook.swift.parser.model.Service

import scala.collection.JavaConverters._

object ThriftService {
  def fromThrift(s: Service) = {
    val methods = s.getMethods.asScala.map(ThriftServiceMethod.fromThrift)
    ThriftService(s.getName, methods)
  }
}

case class ThriftService(
    key: String,
    methods: Seq[ThriftServiceMethod]
)
