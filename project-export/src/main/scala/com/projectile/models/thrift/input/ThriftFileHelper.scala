package com.projectile.models.thrift.input

import com.facebook.swift.parser.model._
import com.projectile.models.export.FieldType
import com.projectile.services.thrift.ThriftParseResult

object ThriftFileHelper {
  def columnTypeFor(t: ThriftType, metadata: ThriftParseResult.Metadata): (String, Seq[String]) = t match {
    case _: VoidType => "Unit" -> Nil
    case i: IdentifierType => colTypeForIdentifier(metadata.typedefs.getOrElse(i.getName, i.getName), metadata)
    case b: BaseType => colTypeForBase(b.getType) -> Nil
    case l: ListType =>
      val v = columnTypeFor(l.getElementType, metadata)
      s"Seq[${v._1}]" -> v._2
    case m: MapType =>
      val k = columnTypeFor(m.getKeyType, metadata)
      val v = columnTypeFor(m.getValueType, metadata)
      s"Map[${k._1}, ${v._1}]" -> (k._2 ++ v._2)
    case s: SetType =>
      val v = columnTypeFor(s.getElementType, metadata)
      s"Set[${v._1}]" -> v._2
    case x => throw new IllegalStateException(s"Unhandled field type [$x]")
  }

  def declarationFor(
    required: Boolean,
    name: String,
    value: Option[ConstValue],
    metadata: ThriftParseResult.Metadata,
    colType: String
  ) = {
    val propType = if (required) { colType } else { "Option[" + colType + "]" }
    s"$name: $propType${propDefault(colType, required, value.map(_.value), metadata.enums)}"
  }

  private[this] def defaultForType(colType: String, enums: Map[String, String]) = colType match {
    case x if x.startsWith("Seq[") => "Nil"
    case x if x.startsWith("Set[") => "Set.empty"
    case x if x.startsWith("Map[") => "Map.empty"
    case x if x.startsWith("Option[") => "None"
    case "Boolean" => "false"
    case "String" => "\"\""
    case "Int" => "0"
    case "Long" => "0L"
    case "Double" => "0.0"
    case x if enums.contains(x) => x + "." + enums(x)
    case x => x + "()"
  }

  private[this] def propDefault(colType: String, required: Boolean, value: Option[Any], enums: Map[String, String]) = value match {
    case Some(_) if required && enums.contains(colType) => " = " + defaultForType(colType, enums)
    case Some(v) if required => " = " + v
    case Some(_) if enums.contains(colType) => " = Some(" + defaultForType(colType, enums) + ")"
    case Some(v) => " = Some(" + v + ")"
    case None if required => " = " + defaultForType(colType, enums)
    case None => " = None"
  }

  private[this] def colTypeForIdentifier(name: String, metadata: ThriftParseResult.Metadata): (String, Seq[String]) = name match {
    case "I64" => FieldType.LongType.asScala -> Nil
    case "I32" => FieldType.IntegerType.asScala -> Nil
    case x if x.contains('.') => x.split('.').toList match {
      case pkg :: cls :: Nil => cls -> metadata.pkgMap.getOrElse(pkg, Nil)
      case _ => throw new IllegalStateException(s"Cannot match [$x].")
    }
    case x => metadata.typedefs.get(x).map(td => colTypeForIdentifier(td, metadata)._1).getOrElse(x) -> Nil
  }

  private[this] def colTypeForBase(t: BaseType.Type) = t match {
    case BaseType.Type.BINARY => FieldType.ByteArrayType.asScala
    case BaseType.Type.BOOL => FieldType.BooleanType.asScala
    case BaseType.Type.BYTE => FieldType.ByteType.asScala
    case BaseType.Type.DOUBLE => FieldType.DoubleType.asScala
    case BaseType.Type.I16 | BaseType.Type.I32 => FieldType.IntegerType.asScala
    case BaseType.Type.I64 => FieldType.LongType.asScala
    case BaseType.Type.STRING => FieldType.StringType.asScala
    case x => throw new IllegalStateException(s"Unhandled base type [$x]")
  }
}
