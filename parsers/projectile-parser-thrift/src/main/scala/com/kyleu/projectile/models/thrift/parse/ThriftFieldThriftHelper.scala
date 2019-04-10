package com.kyleu.projectile.models.thrift.parse

import com.kyleu.projectile.models.export.ExportField
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.output.ExportHelper
import com.kyleu.projectile.models.thrift.input.{ThriftFileHelper, ThriftInput}
import com.kyleu.projectile.models.thrift.schema.ThriftStructField

object ThriftFieldThriftHelper {
  def getAsThrift(field: ThriftStructField, input: ThriftInput) = {
    val t = ThriftFileHelper.columnTypeFor(field.t, input)
    parse(field.name, t, field.required)
  }

  def getFromField(field: ExportField) = {
    parse(ExportHelper.escapeKeyword(field.propertyName), field.t, field.required)
  }

  private[this] def parse(name: String, t: FieldType, required: Boolean): String = t match {
    case FieldType.MapType(_, v) =>
      val valuesMapped = parseMapped(v, "map", "mapValues")
      if (required) {
        s"$name$valuesMapped"
      } else if (valuesMapped.isEmpty) {
        s"$name"
      } else {
        s"$name.map(x => x$valuesMapped)"
      }
    case FieldType.ListType(typ) =>
      val mapped = parseMapped(typ, "seq")
      if (required) {
        s"$name$mapped"
      } else if (mapped.isEmpty) {
        s"$name"
      } else {
        s"$name.map(x => x$mapped)"
      }
    case FieldType.SetType(typ) =>
      val mapped = parseMapped(typ, "set")
      if (required) {
        s"$name$mapped.toSet"
      } else if (mapped.isEmpty) {
        s"$name.map(_.toSet)"
      } else {
        s"$name.map(x => x$mapped.toSet)"
      }
    case _ if FieldType.scalars.apply(t) => name
    case _ if required => s"$name.asThrift"
    case _ => s"$name.map(_.asThrift)"
  }

  private[this] def parseMapped(t: FieldType, ctx: String, key: String = "map"): String = t match {
    case FieldType.MapType(_, _) => throw new IllegalStateException(s"Unhandled [$ctx] child Map")
    case FieldType.ListType(typ) => typ match {
      case FieldType.StructType(_, _) => s".$key(_${parseMapped(typ, ctx)})"
      case FieldType.EnumType(_) => s".$key(_${parseMapped(typ, ctx)})"
      case _ => ""
    }
    case FieldType.SetType(_) => throw new IllegalStateException(s"Unhandled [$ctx] child Set")
    case _ if FieldType.scalars.apply(t) => ""
    case _ => s".$key(_.asThrift)"
  }
}
