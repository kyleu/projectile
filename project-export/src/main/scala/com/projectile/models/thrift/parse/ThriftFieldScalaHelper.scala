package com.projectile.models.thrift.parse

import com.projectile.models.export.ExportField
import com.projectile.models.export.typ.FieldType
import com.projectile.models.thrift.input.{ThriftFileHelper, ThriftInput}
import com.projectile.models.thrift.schema.ThriftStructField

object ThriftFieldScalaHelper {
  def getFromThrift(field: ThriftStructField, input: ThriftInput) = {
    val t = ThriftFileHelper.columnTypeFor(field.t, input)
    parse("t", field.name, t, field.required || field.value.isDefined)
  }

  def getFromField(field: ExportField) = {
    parse("t", field.propertyName, field.t, field.required)
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
        s"$root.$name$mapped.toList"
      } else {
        s"$root.$name.map(x => x$mapped.toList)"
      }
    case FieldType.SetType(typ) =>
      val mapped = parseMapped(typ, "set")
      if (required) {
        s"$root.$name$mapped.toSet"
      } else {
        s"$root.$name.map(x => x$mapped.toSet)"
      }
    case _ if FieldType.scalars.apply(t) => s"$root.$name"

    case FieldType.EnumType(key) if required => s"$key.fromThrift($root.$name)"
    case FieldType.EnumType(key) => s"$root.$name.map($key.fromThrift)"

    case FieldType.StructType(key) if required => s"$key.fromThrift($root.$name)"
    case FieldType.StructType(key) => s"$root.$name.map($key.fromThrift)"

    case _ => throw new IllegalStateException(s"Unhandled type [${t.toString}")
  }

  private[this] def parseMapped(t: FieldType, ctx: String): String = t match {
    case FieldType.MapType(_, _) => throw new IllegalStateException(s"Unhandled [$ctx] child Map")
    case FieldType.ListType(_) => throw new IllegalStateException(s"Unhandled [$ctx] child Seq")
    case FieldType.SetType(_) => throw new IllegalStateException(s"Unhandled [$ctx] child Set")
    case _ if FieldType.scalars.apply(t) => ""
    case FieldType.EnumType(key) => s".map($key.fromThrift)"
    case FieldType.StructType(key) => s".map($key.fromThrift)"
    case _ => throw new IllegalStateException(s"Unhandled nested type [${t.toString}")
  }
}
