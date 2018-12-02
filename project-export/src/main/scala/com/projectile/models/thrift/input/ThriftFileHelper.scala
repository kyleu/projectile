package com.projectile.models.thrift.input

import com.facebook.swift.parser.model._
import com.projectile.models.export.typ.FieldType
import com.projectile.models.export.{ExportEnum, ExportField}
import com.projectile.models.output.ExportHelper
import com.projectile.services.thrift.ThriftParseResult

object ThriftFileHelper {
  def columnTypeFor(t: ThriftType, metadata: ThriftParseResult.Metadata): FieldType = t match {
    case _: VoidType => FieldType.UnitType
    case i: IdentifierType => colTypeForIdentifier(metadata.typedefs.getOrElse(i.getName, i.getName), metadata)
    case b: BaseType => colTypeForBase(b.getType)
    case l: ListType => FieldType.ListType(columnTypeFor(l.getElementType, metadata))
    case s: SetType =>
      FieldType.SetType(columnTypeFor(s.getElementType, metadata))
    case m: MapType =>
      val k = columnTypeFor(m.getKeyType, metadata)
      val v = columnTypeFor(m.getValueType, metadata)
      FieldType.MapType(k, v)
    case x => throw new IllegalStateException(s"Unhandled field type [$x]")
  }

  def declarationForField(field: ExportField, enums: Seq[ExportEnum]) = {
    declarationFor(field.notNull, field.propertyName, field.defaultValue, field.t, enums)
  }

  def declarationFor(
    required: Boolean,
    name: String,
    value: Option[String],
    colType: FieldType,
    enums: Seq[ExportEnum]
  ) = {
    val propType = if (required) { colType } else { "Option[" + colType + "]" }
    s"$name: $propType${propDefault(colType, required, value, enums)}"
  }

  private[this] def defaultForType(colType: FieldType, required: Boolean, enums: Seq[ExportEnum]) = colType match {
    case _ if !required => "None"
    case FieldType.ListType(_) => "Nil"
    case FieldType.SetType(_) => "Set.empty"
    case FieldType.MapType(_, _) => "Map.empty"
    case FieldType.BooleanType => "false"
    case FieldType.StringType => "\"\""
    case FieldType.IntegerType => "0"
    case FieldType.LongType => "0L"
    case FieldType.DoubleType => "0.0"
    case FieldType.EnumType(k) => enums.find(_.key == k) match {
      case Some(e) => e.className + "." + ExportHelper.toClassName(e.values.head.indexOf(':') match {
        case -1 => e.values.head
        case v => e.values.head.substring(v + 1)
      })
      case None => throw new IllegalStateException(s"No enum with key [$k]")
    }
    case x => x + "()"
  }

  private[this] def propDefault(colType: FieldType, required: Boolean, value: Option[Any], enums: Seq[ExportEnum]) = value match {
    case Some(v) if required => colType match {
      case FieldType.EnumType(_) => " = " + defaultForType(colType, required, enums)
      case _ => " = " + v
    }
    case Some(v) => colType match {
      case FieldType.EnumType(_) => " = Some(" + defaultForType(colType, required, enums) + ")"
      case _ => " = Some(" + v + ")"
    }
    case None => " = " + defaultForType(colType, required, enums)
  }

  private[this] def colTypeForIdentifier(name: String, metadata: ThriftParseResult.Metadata): FieldType = name match {
    case "I64" => FieldType.LongType
    case "I32" => FieldType.IntegerType
    case x if x.contains('.') => x.split('.').toList match {
      case pkg :: cls :: Nil => throw new IllegalStateException(s"Col type error: [$pkg], [$cls]")
      case cls :: Nil => throw new IllegalStateException(s"Col type error: [$cls]")
      case _ => throw new IllegalStateException(s"Cannot match [$x].")
    }
    case x => metadata.typedefs.get(x).map(td => colTypeForIdentifier(td, metadata)).getOrElse(FieldType.StringType)
  }

  private[this] def colTypeForBase(t: BaseType.Type) = t match {
    case BaseType.Type.BINARY => FieldType.ByteArrayType
    case BaseType.Type.BOOL => FieldType.BooleanType
    case BaseType.Type.BYTE => FieldType.ByteType
    case BaseType.Type.DOUBLE => FieldType.DoubleType
    case BaseType.Type.I16 | BaseType.Type.I32 => FieldType.IntegerType
    case BaseType.Type.I64 => FieldType.LongType
    case BaseType.Type.STRING => FieldType.StringType
    case x => throw new IllegalStateException(s"Unhandled base type [$x]")
  }
}
