package com.projectile.models.thrift.schema

import com.facebook.swift.parser.model.{ConstValue, ThriftField, ThriftType}
import com.facebook.swift.parser.model.ThriftField.Requiredness

object ThriftStructField {
  protected val renames = Map("type" -> "`type`")

  def fromThrift(f: ThriftField) = {
    val key = f.getName
    val name = ThriftStructField.renames.getOrElse(key, key)
    val required = f.getRequiredness != Requiredness.OPTIONAL
    val t = f.getType
    val value = Option(f.getValue.orNull)
    ThriftStructField(key, name, required, t, value)
  }
}

case class ThriftStructField(key: String, name: String, required: Boolean, t: ThriftType, value: Option[ConstValue])
