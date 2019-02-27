package com.kyleu.projectile.models.thrift.parse

import com.kyleu.projectile.models.export.ExportField
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.thrift.input.{ThriftFileHelper, ThriftInput}
import com.kyleu.projectile.models.thrift.schema.ThriftStructField

object ThriftFieldScalaHelper {
  def getFromThrift(field: ThriftStructField, input: ThriftInput) = {
    val t = ThriftFileHelper.columnTypeFor(field.t, input)
    parse("t", field.name, t, field.required)
  }

  def getFromField(field: ExportField) = {
    parse("t", field.propertyName, field.t, field.required)
  }

  private[this] def parse(root: String, name: String, t: FieldType, required: Boolean): String = t match {
    case FieldType.MapType(_, v) =>
      val valuesMapped = parseMapped(v, "map", key = "mapValues")
      if (required) {
        s"$root.$name$valuesMapped.toMap"
      } else if (valuesMapped.isEmpty) {
        s"$root.$name.map(_.toMap)"
      } else {
        s"$root.$name.map(x => x$valuesMapped.toMap)"
      }
    case FieldType.ListType(typ) =>
      val mapped = parseMapped(typ, "seq")
      if (required) {
        s"$root.$name$mapped.toList"
      } else if (mapped.isEmpty) {
        s"$root.$name.map(_.toList)"
      } else {
        s"$root.$name.map(x => x$mapped.toList)"
      }
    case FieldType.SetType(typ) =>
      val mapped = parseMapped(typ, "set")
      if (required) {
        s"$root.$name$mapped.toSet"
      } else if (mapped.isEmpty) {
        s"$root.$name.map(_.toSet)"
      } else {
        s"$root.$name.map(x => x$mapped.toSet)"
      }

    case FieldType.UnionType(typ, _) => s"$root.$name"
    case _ if FieldType.scalars.apply(t) => s"$root.$name"

    case FieldType.EnumType(key) if required => s"$key.fromThrift($root.$name)"
    case FieldType.EnumType(key) => s"$root.$name.map($key.fromThrift)"

    case FieldType.StructType(key) if required => s"$key.fromThrift($root.$name)"
    case FieldType.StructType(key) => s"$root.$name.map($key.fromThrift)"

    case _ => throw new IllegalStateException(s"Unhandled type [${t.toString}")
  }

  private[this] def parseMapped(t: FieldType, ctx: String, key: String = "map"): String = t match {
    case FieldType.MapType(_, v) => parseMapped(v, ctx + ".map", key = "mapValues") match {
      case x if x.isEmpty => s".$key(_$x)"
      case x => s".$key(_$x)"
    }
    case FieldType.ListType(typ) => parseMapped(typ, ctx + ".list") match {
      case x if x.isEmpty => s".$key(_$x.toList)"
      case x => s".$key(_$x.toList)"
    }
    case FieldType.SetType(typ) => parseMapped(typ, ctx + ".set") match {
      case x if x.isEmpty => s".$key(_$x.toSet)"
      case x => s".$key(_$x.toSet)"
    }
    case FieldType.UnionType(_, _) => ""
    case _ if FieldType.scalars.apply(t) => ""
    case FieldType.EnumType(k) => s".$key($k.fromThrift)"
    case FieldType.StructType(k) => s".$key($k.fromThrift)"
    case _ => throw new IllegalStateException(s"Unhandled nested type [${t.toString}")
  }
}
