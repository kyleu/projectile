package com.kyleu.projectile.models.thrift.input

import com.facebook.swift.parser.model._
import com.kyleu.projectile.models.export.ExportField
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.{FieldType, FieldTypeAsScala}
import com.kyleu.projectile.models.output.ExportHelper

object ThriftFileHelper {
  def columnTypeFor(t: ThriftType, input: ThriftInput): FieldType = t match {
    case _: VoidType => FieldType.UnitType
    case i: IdentifierType => ThriftTypeHelper.colTypeForIdentifier(input.typedefs.getOrElse(i.getName, i.getName), input)
    case b: BaseType => ThriftTypeHelper.colTypeForBase(b.getType)
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
    val propType = if (required) {
      FieldTypeAsScala.asScala(config, colType, isThrift = true)
    } else {
      "Option[" + FieldTypeAsScala.asScala(config, colType, isThrift = true) + "]"
    }
    s"${ExportHelper.escapeKeyword(name)}: $propType${ThriftTypeHelper.propDefault(config, colType, required, value)}"
  }
}
