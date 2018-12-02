package com.projectile.models.thrift.parse

import com.projectile.models.export.ExportField
import com.projectile.models.export.typ.FieldType
import com.projectile.models.thrift.input.ThriftFileHelper
import com.projectile.models.thrift.schema.ThriftStructField
import com.projectile.services.thrift.ThriftParseResult

object ThriftFieldScalaHelper {
  def getFromThrift(field: ThriftStructField, metadata: ThriftParseResult.Metadata) = {
    val t = ThriftFileHelper.columnTypeFor(field.t, metadata)
    parse("t", field.name, t, field.required || field.value.isDefined)
  }

  def getFromField(field: ExportField) = {
    parse("t", field.propertyName, field.t, field.notNull)
  }

  private[this] def parse(root: String, name: String, t: FieldType, required: Boolean): String = t match {
    case FieldType.MapType(_, v) =>
      val valuesMapped = parseMapped(v, "map").replaceAllLiterally(".map", ".mapValues(")
      if (required) {
        s"$root.$name$valuesMapped.toMap"
      } else {
        s"$root.$name.map(x => x$valuesMapped).toMap"
      }
    case FieldType.ListType(typ) =>
      val mapped = parseMapped(typ, "seq")
      if (required) {
        s"$root.$name$mapped"
      } else {
        s"$root.$name.map(x => x$mapped)"
      }
    case FieldType.SetType(typ) =>
      val mapped = parseMapped(typ, "set")
      if (required) {
        s"$root.$name$mapped.toSet"
      } else {
        s"$root.$name.map(x => x$mapped.toSet)"
      }
    case _ if FieldType.scalars.apply(t) => s"$root.$name"
    case _ if required => s"$t.fromThrift($root.$name)"
    case _ => s"$root.$name.map($t.fromThrift)"
  }

  private[this] def parseMapped(t: FieldType, ctx: String): String = t match {
    case FieldType.MapType(_, _) => throw new IllegalStateException(s"Unhandled [$ctx] child Map")
    case FieldType.ListType(_) => "" // throw new IllegalStateException(s"Unhandled [$ctx] child Seq")
    case FieldType.SetType(_) => throw new IllegalStateException(s"Unhandled [$ctx] child Set")
    case _ if FieldType.scalars.apply(t) => ""
    case x => s".map($x.fromThrift)"
  }
}
