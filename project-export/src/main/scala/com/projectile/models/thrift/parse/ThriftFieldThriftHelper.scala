package com.projectile.models.thrift.parse

import com.projectile.models.export.ExportField
import com.projectile.models.export.typ.FieldType
import com.projectile.models.thrift.input.{ThriftFileHelper, ThriftInput}
import com.projectile.models.thrift.schema.ThriftStructField

object ThriftFieldThriftHelper {
  def getAsThrift(field: ThriftStructField, input: ThriftInput) = {
    val t = ThriftFileHelper.columnTypeFor(field.t, input)
    parse(field.name, t, field.required || field.value.isDefined)
  }

  def getFromField(field: ExportField) = {
    parse(field.propertyName, field.t, field.required)
  }

  private[this] def parse(name: String, t: FieldType, required: Boolean): String = t match {
    case FieldType.MapType(_, v) =>
      val valuesMapped = parseMapped(v, "map").replaceAllLiterally(".map", ".mapValues(")
      if (required) {
        s"$name$valuesMapped.toMap"
      } else {
        s"$name.map(x => x$valuesMapped).toMap"
      }
    case FieldType.ListType(typ) =>
      val mapped = parseMapped(typ, "seq")
      if (required) {
        s"$name$mapped"
      } else {
        s"$name.map(x => x$mapped)"
      }
    case FieldType.SetType(typ) =>
      val mapped = parseMapped(typ, "set")
      if (required) {
        s"$name$mapped.toSet"
      } else {
        s"$name.map(x => x$mapped.toSet)"
      }
    case _ if FieldType.scalars.apply(t) => name
    case _ if required => s"$name.asThrift"
    case _ => s"$name.map(_.asThrift)"
  }

  private[this] def parseMapped(t: FieldType, ctx: String): String = t match {
    case FieldType.MapType(_, _) => throw new IllegalStateException(s"Unhandled [$ctx] child Map")
    case FieldType.ListType(_) => "" // throw new IllegalStateException(s"Unhandled [$ctx] child Seq")
    case FieldType.SetType(_) => throw new IllegalStateException(s"Unhandled [$ctx] child Set")
    case _ if FieldType.scalars.apply(t) => ""
    case _ => s".map(_.asThrift)"
  }
}
