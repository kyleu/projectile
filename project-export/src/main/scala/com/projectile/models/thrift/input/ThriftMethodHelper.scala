package com.projectile.models.thrift.input

import com.projectile.models.export.ExportField
import com.projectile.models.export.typ.FieldType

object ThriftMethodHelper {
  def getReturnMapping(t: FieldType): String = t match {
    case _ if FieldType.scalars(t) => ""
    case FieldType.MapType(_, v) => getReturnSubMapping(v) match {
      case r if r.isEmpty => ".map(_.toMap)"
      case r => s".map(_.mapValues($r).toMap)"
    }
    case FieldType.ListType(typ) => getReturnSubMapping(typ) match {
      case r if r.isEmpty => ""
      case r => s".map(_.map($r).toList)"
    }
    case FieldType.SetType(typ) => getReturnSubMapping(typ) match {
      case r if r.isEmpty => ".map(_.toSet)"
      case r => s".map(_.map($r).toSet)"
    }
    case FieldType.EnumType(key) => s".map($key.fromThrift)"
    case FieldType.StructType(key) => s".map($key.fromThrift)"
    case FieldType.UnitType => ""
    case _ => throw new IllegalStateException(s"Unhandled return type [${t.toString}")
  }

  def getArgCall(field: ExportField) = parse(field.propertyName, field.t, field.required)

  private[this] def getReturnSubMapping(t: FieldType): String = t match {
    case _ if FieldType.scalars(t) => ""
    case FieldType.MapType(_, v) => getReturnSubMapping(v) match {
      case r if r.isEmpty => "_.toMap"
      case r => s"_.mapValues($r).toMap"
    }
    case FieldType.ListType(typ) => getReturnSubMapping(typ) match {
      case r if r.isEmpty => "_.toList"
      case r => s"_.map($r).toList"
    }
    case FieldType.SetType(typ) => getReturnSubMapping(typ) match {
      case r if r.isEmpty => "_.toSet"
      case r => s"_.map($r).toSet"
    }
    case FieldType.EnumType(key) => s"$key.fromThrift"
    case FieldType.StructType(key) => s"$key.fromThrift"
    case _ => throw new IllegalStateException(s"Unhandled nested type [${t.toString}")
  }

  private[this] def parse(name: String, t: FieldType, required: Boolean): String = t match {
    case FieldType.MapType(_, v) =>
      val valuesMapped = parseMapped(v, "map").replaceAllLiterally(".map", ".mapValues(")
      if (required) {
        s"$name$valuesMapped.toMap"
      } else {
        s"$name.map(_$valuesMapped).toMap"
      }
    case FieldType.ListType(typ) =>
      val mapped = parseMapped(typ, "seq")
      if (mapped.isEmpty) {
        s"$name"
      } else {
        s"$name.map(_$mapped)"
      }
    case FieldType.SetType(typ) =>
      val mapped = parseMapped(typ, "set")
      if (mapped.isEmpty) {
        s"$name"
      } else {
        s"$name.map(_$mapped)"
      }
    case _ if FieldType.scalars(t) => s"$name"
    case _ if required => s"$name.asThrift"
    case _ => s"$name.map(_.asThrift)"
  }

  private[this] def parseMapped(t: FieldType, ctx: String): String = t match {
    case FieldType.MapType(_, _) => throw new IllegalStateException(s"Unhandled [$ctx] child Map")
    case FieldType.ListType(_) => throw new IllegalStateException(s"Unhandled [$ctx] child Seq")
    case FieldType.SetType(_) => throw new IllegalStateException(s"Unhandled [$ctx] child Set")
    case _ if FieldType.scalars(t) => ""
    case _ => s".asThrift"
  }
}
