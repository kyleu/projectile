package com.kyleu.projectile.models.thrift.input

import com.facebook.swift.parser.model._
import com.kyleu.projectile.models.export.ExportField
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType.StructType
import com.kyleu.projectile.models.export.typ.{FieldType, FieldTypeAsScala}
import com.kyleu.projectile.models.output.ExportHelper
import com.kyleu.projectile.util.StringUtils

object ThriftFileHelper {
  def columnTypeFor(t: ThriftType, input: ThriftInput): FieldType = t match {
    case _: VoidType => FieldType.UnitType
    case i: IdentifierType => colTypeForIdentifier(input.typedefs.getOrElse(i.getName, i.getName), input)
    case b: BaseType => colTypeForBase(b.getType)
    case l: ListType => FieldType.ListType(columnTypeFor(l.getElementType, input))
    case s: SetType =>
      FieldType.SetType(columnTypeFor(s.getElementType, input))
    case m: MapType =>
      val k = columnTypeFor(m.getKeyType, input)
      val v = columnTypeFor(m.getValueType, input)
      FieldType.MapType(k, v)
    case x => throw new IllegalStateException(s"Unhandled field type [$x]")
  }

  def declarationForField(config: ExportConfiguration, field: ExportField) = {
    declarationFor(config, field.required, field.propertyName, field.defaultValue, field.t)
  }

  def declarationFor(
    config: ExportConfiguration,
    required: Boolean,
    name: String,
    value: Option[String],
    colType: FieldType
  ) = {
    val propType = if (required) { FieldTypeAsScala.asScala(config, colType) } else { "Option[" + FieldTypeAsScala.asScala(config, colType) + "]" }
    s"$name: $propType${propDefault(config, colType, required, value)}"
  }

  private[this] def defaultForType(config: ExportConfiguration, colType: FieldType, required: Boolean): String = colType match {
    case _ if !required => "None"
    case FieldType.ListType(_) => "Nil"
    case FieldType.SetType(_) => "Set.empty"
    case FieldType.MapType(_, _) => "Map.empty"
    case FieldType.BooleanType => "false"
    case FieldType.StringType => "\"\""
    case FieldType.IntegerType => "0"
    case FieldType.LongType => "0L"
    case FieldType.FloatType | FieldType.DoubleType => "0.0"
    case FieldType.EnumType(k) => config.getEnumOpt(k) match {
      case Some(e) => e.className + "." + ExportHelper.toClassName(e.values.headOption.getOrElse(throw new IllegalStateException()).indexOf(':') match {
        case -1 => e.values.headOption.getOrElse(throw new IllegalStateException())
        case v => e.values.headOption.getOrElse(throw new IllegalStateException()).substring(v + 1)
      })
      case None => throw new IllegalStateException(s"No enum with key [$k]")
    }
    case FieldType.StructType(k) => config.getModelOpt(k) match {
      case Some(m) => m.className + "()"
      case None => throw new IllegalStateException(s"No model with key [$k]")
    }
    case x => x + "()"
  }

  private[this] def propDefault(config: ExportConfiguration, colType: FieldType, required: Boolean, value: Option[String]) = value match {
    case Some(v) if required => colType match {
      case FieldType.EnumType(_) => " = " + defaultForType(config, colType, required)
      case FieldType.StructType(_) => " = " + defaultForType(config, colType, required)
      case _ => " = " + v
    }
    case Some(v) => colType match {
      case FieldType.EnumType(_) => " = " + defaultForType(config, colType, required)
      case _ => " = Some(" + v + ")"
    }
    case None => " = " + defaultForType(config, colType, required)
  }

  private[this] def typeForClass(cls: String, input: ThriftInput): FieldType = input.getEnumOpt(cls).map(e => FieldType.EnumType(e.key)).orElse {
    if (input.exportModelNames.contains(cls)) { Some(StructType(cls)) } else { None }
  }.orElse {
    input.typedefs.get(cls).map(colTypeForIdentifier(_, input))
  }.getOrElse {
    throw new IllegalStateException(s"Col type error: no enum or model found with key [$cls]")
  }

  private[this] def colTypeForIdentifier(name: String, input: ThriftInput): FieldType = name match {
    case "I64" => FieldType.LongType
    case "I32" => FieldType.IntegerType
    case x if x.contains('.') => StringUtils.toList(x, '.') match {
      case _ :: cls :: Nil => typeForClass(cls, input)
      case cls :: Nil => typeForClass(cls, input)
      case _ => throw new IllegalStateException(s"Cannot match [$x]")
    }
    case x => typeForClass(x, input)
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
