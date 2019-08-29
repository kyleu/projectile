package com.kyleu.projectile.models.thrift.input

import com.facebook.swift.parser.model._
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.export.typ.FieldType.{StructType, UnionType}
import com.kyleu.projectile.util.StringUtils

object ThriftTypeHelper {
  private[this] def defaultForType(config: ExportConfiguration, colType: FieldType, required: Boolean): String = colType match {
    case _ if !required => "None"
    case FieldType.UnionType(_, _) => "null"
    case FieldType.ListType(_) => "Nil"
    case FieldType.SetType(_) => "Set.empty"
    case FieldType.MapType(_, _) => "Map.empty"
    case FieldType.BooleanType => "false"
    case FieldType.StringType => "\"\""
    case FieldType.IntegerType => "0"
    case FieldType.LongType => "0L"
    case FieldType.FloatType | FieldType.DoubleType => "0.0"
    case FieldType.EnumType(k) => config.getEnumOpt(k) match {
      case Some(e) => e.className + "." + e.firstVal.className
      case None => throw new IllegalStateException(s"No enum with key [$k]")
    }
    case FieldType.StructType(k, _) => config.getModelOpt(k) match {
      case Some(m) => m.className + "()"
      case None => throw new IllegalStateException(s"No model with key [$k]")
    }
    case x => x.toString + "()"
  }

  def propDefault(config: ExportConfiguration, colType: FieldType, required: Boolean, value: Option[String]) = value match {
    case Some(v) if required => colType match {
      case FieldType.UnionType(_, _) => ""
      case FieldType.EnumType(_) => " = " + defaultForType(config, colType, required)
      case FieldType.StructType(_, _) => " = " + defaultForType(config, colType, required)
      case _ => " = " + v
    }
    case Some(v) => colType match {
      case FieldType.EnumType(_) => " = " + defaultForType(config, colType, required)
      case _ => " = Some(" + v + ")"
    }
    case None => " = " + defaultForType(config, colType, required)
  }

  private[this] def typeForClass(cls: String, input: ThriftInput): FieldType = input.enumOpt(cls).map(e => FieldType.EnumType(e.key)).orElse {
    if (input.exportModelNames.contains(cls)) { Some(StructType(cls, Nil)) } else { None }
  }.orElse {
    input.typedefs.get(cls).map(colTypeForIdentifier(_, input))
  }.orElse {
    if (input.exportUnionNames.contains(cls)) { Some(UnionType(cls, Nil)) } else { None }
  }.getOrElse {
    throw new IllegalStateException(s"Col type error: no enum, model, or union found with key [$cls]")
  }

  def colTypeForIdentifier(name: String, input: ThriftInput): FieldType = name match {
    case "I64" => FieldType.LongType
    case "I32" => FieldType.IntegerType
    case x if x.contains('.') => StringUtils.toList(x, '.') match {
      case _ :: cls :: Nil => typeForClass(cls, input)
      case cls :: Nil => typeForClass(cls, input)
      case _ => throw new IllegalStateException(s"Cannot match [$x]")
    }
    case x => typeForClass(x, input)
  }

  def colTypeForBase(t: BaseType.Type) = t match {
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
