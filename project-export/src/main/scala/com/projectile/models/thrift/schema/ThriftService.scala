package com.projectile.models.thrift.schema

import com.facebook.swift.parser.model.Service
import com.projectile.util.JsonSerializers._

import scala.collection.JavaConverters._

object ThriftService {
  implicit val jsonEncoder: Encoder[ThriftService] = deriveEncoder
  implicit val jsonDecoder: Decoder[ThriftService] = deriveDecoder

  def fromThrift(s: Service) = {
    val methods = s.getMethods.asScala.map(ThriftServiceMethod.fromThrift)
    ThriftService(s.getName, methods)
  }
}

case class ThriftService(
    key: String,
    methods: Seq[ThriftServiceMethod]
)
