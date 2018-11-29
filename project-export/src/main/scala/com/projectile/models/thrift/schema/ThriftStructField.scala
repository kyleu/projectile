package com.projectile.models.thrift.schema

import com.facebook.swift.parser.model.{ConstValue, ThriftField, ThriftType}
import com.facebook.swift.parser.model.ThriftField.Requiredness

object ThriftStructField {
  private[this] val renames = Map("type" -> "`type`")

  private[this] def fromKeyTypeVal(key: String, required: Boolean, t: ThriftType, v: Option[ConstValue]) = {
    val name = renames.getOrElse(key, key)
    ThriftStructField(key, name, required, t, v)
  }

  def fromThrift(f: ThriftField) = {
    fromKeyTypeVal(key = f.getName, required = f.getRequiredness != Requiredness.OPTIONAL, t = f.getType, v = Option(f.getValue.orNull))
  }
}

case class ThriftStructField(key: String, name: String, required: Boolean, t: ThriftType, value: Option[ConstValue])
