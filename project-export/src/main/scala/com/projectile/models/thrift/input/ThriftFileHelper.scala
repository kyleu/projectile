package com.projectile.models.thrift.input

import com.facebook.swift.parser.model._
import com.projectile.models.export.{ExportEnum, FieldType}
import com.projectile.models.output.ExportHelper
import com.projectile.services.thrift.ThriftParseResult

object ThriftFileHelper {
  def columnTypeFor(t: ThriftType, metadata: ThriftParseResult.Metadata): (FieldType, String) = t match {
    case _: VoidType => FieldType.ComplexType -> "Unit"
    case i: IdentifierType => colTypeForIdentifier(metadata.typedefs.getOrElse(i.getName, i.getName), metadata)
    case b: BaseType => colTypeForBase(b.getType)
    case l: ListType => FieldType.ComplexType -> s"Seq[${columnTypeFor(l.getElementType, metadata)._2}]"
    case s: SetType => FieldType.ComplexType -> s"Set[${columnTypeFor(s.getElementType, metadata)._2}]"
    case m: MapType =>
      val k = columnTypeFor(m.getKeyType, metadata)._2
      val v = columnTypeFor(m.getValueType, metadata)._2
      FieldType.ComplexType -> s"Map[$k, $v]"
    case x => throw new IllegalStateException(s"Unhandled field type [$x]")
  }

  def declarationFor(
    required: Boolean,
    name: String,
    value: Option[String],
    colType: String,
    enums: Seq[ExportEnum]
  ) = {
    val propType = if (required) { colType } else { "Option[" + colType + "]" }
    s"$name: $propType${propDefault(colType, required, value, enums)}"
  }

  private[this] def defaultForType(colType: String, enums: Seq[ExportEnum]) = colType match {
    case x if x.startsWith("Seq[") => "Nil"
    case x if x.startsWith("Set[") => "Set.empty"
    case x if x.startsWith("Map[") => "Map.empty"
    case x if x.startsWith("Option[") => "None"
    case "Boolean" => "false"
    case "String" => "\"\""
    case "Int" => "0"
    case "Long" => "0L"
    case "Double" => "0.0"
    case x => enums.find(_.key == x) match {
      case Some(e) => e.className + "." + ExportHelper.toClassName(e.values.head.indexOf(':') match {
        case -1 => e.values.head
        case v => e.values.head.substring(v + 1)
      })
      case None => x + "()"
    }
  }

  private[this] def propDefault(colType: String, required: Boolean, value: Option[Any], enums: Seq[ExportEnum]) = value match {
    case Some(_) if required && enums.exists(_.key == colType) => " = " + defaultForType(colType, enums)
    case Some(v) if required => " = " + v
    case Some(_) if enums.exists(_.key == colType) => " = Some(" + defaultForType(colType, enums) + ")"
    case Some(v) => " = Some(" + v + ")"
    case None if required => " = " + defaultForType(colType, enums)
    case None => " = None"
  }

  private[this] def ft(x: FieldType) = x -> x.asScala

  private[this] def colTypeForIdentifier(name: String, metadata: ThriftParseResult.Metadata): (FieldType, String) = name match {
    case "I64" => ft(FieldType.LongType)
    case "I32" => ft(FieldType.IntegerType)
    case x if x.contains('.') => x.split('.').toList match {
      case pkg :: cls :: Nil => FieldType.ComplexType -> (metadata.pkgMap.getOrElse(pkg, Nil) :+ cls).mkString(".")
      case _ => throw new IllegalStateException(s"Cannot match [$x].")
    }
    case x => metadata.typedefs.get(x).map(td => colTypeForIdentifier(td, metadata)).getOrElse(FieldType.ComplexType -> x)
  }

  private[this] def colTypeForBase(t: BaseType.Type) = t match {
    case BaseType.Type.BINARY => ft(FieldType.ByteArrayType)
    case BaseType.Type.BOOL => ft(FieldType.BooleanType)
    case BaseType.Type.BYTE => ft(FieldType.ByteType)
    case BaseType.Type.DOUBLE => ft(FieldType.DoubleType)
    case BaseType.Type.I16 | BaseType.Type.I32 => ft(FieldType.IntegerType)
    case BaseType.Type.I64 => ft(FieldType.LongType)
    case BaseType.Type.STRING => ft(FieldType.StringType)
    case x => throw new IllegalStateException(s"Unhandled base type [$x]")
  }
}
