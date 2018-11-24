package com.projectile.models.thrift.schema

import com.facebook.swift.parser.model.ThriftField
import com.facebook.swift.parser.model.ThriftField.Requiredness
import com.projectile.util.JsonSerializers._

object ThriftStructField {
  implicit val jsonEncoder: Encoder[ThriftStructField] = deriveEncoder
  implicit val jsonDecoder: Decoder[ThriftStructField] = deriveDecoder

  protected val renames = Map("type" -> "`type`")

  def fromThrift(f: ThriftField) = {
    val key = f.getName
    val name = ThriftStructField.renames.getOrElse(key, key)
    val required = f.getRequiredness != Requiredness.OPTIONAL
    val t = f.getType
    val value = Option(f.getValue.orNull)
    ThriftStructField(key, name, required, t.toString, value.map(_.toString))
  }
}

case class ThriftStructField(key: String, name: String, required: Boolean, t: String, value: Option[String])
