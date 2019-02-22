package com.kyleu.projectile.models.thrift.schema

import com.facebook.swift.parser.model.{ThriftField, ThriftType}
import com.kyleu.projectile.models.export.ExportField
import com.kyleu.projectile.models.output.ExportHelper
import com.kyleu.projectile.models.thrift.input.{ThriftFileHelper, ThriftInput}

object ThriftUnionMember {
  def fromThrift(f: ThriftField) = {
    ThriftUnionMember(key = f.getName, t = f.getType)
  }
}

case class ThriftUnionMember(key: String, t: ThriftType) {
  def asField(input: ThriftInput) = {
    val typ = ThriftFileHelper.columnTypeFor(t, input)
    ExportField(key = key, propertyName = ExportHelper.toIdentifier(key), title = ExportHelper.toClassName(key), description = None, t = typ)
  }
}
