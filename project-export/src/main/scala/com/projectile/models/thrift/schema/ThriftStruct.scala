package com.projectile.models.thrift.schema

import com.facebook.swift.parser.model.Struct
import com.projectile.util.JsonSerializers._

import scala.collection.JavaConverters._

object ThriftStruct {
  implicit val jsonEncoder: Encoder[ThriftStruct] = deriveEncoder
  implicit val jsonDecoder: Decoder[ThriftStruct] = deriveDecoder

  def fromThrift(s: Struct) = {
    val key = s.getName
    val fields = s.getFields.asScala.map(ThriftStructField.fromThrift)
    ThriftStruct(key, fields)
  }
}

case class ThriftStruct(key: String, fields: Seq[ThriftStructField])

