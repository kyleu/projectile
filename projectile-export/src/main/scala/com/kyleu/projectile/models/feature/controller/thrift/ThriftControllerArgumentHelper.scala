package com.kyleu.projectile.models.feature.controller.thrift

import com.kyleu.projectile.models.export.ExportMethod
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType

object ThriftControllerArgumentHelper {
  def defaultArgs(m: ExportMethod, config: ExportConfiguration) = {
    val argsMapped = m.args.map { arg =>
      s""""${arg.key}" -> ${getDefault(arg.t, config)}.asJson"""
    }.mkString(", ")
    s"Json.obj($argsMapped)"
  }

  private[this] def getDefault(t: FieldType, config: ExportConfiguration): String = t match {
    case FieldType.UnitType => "null"
    case FieldType.ByteArrayType => "null"
    case FieldType.BooleanType => "false"
    case FieldType.ByteType | FieldType.IntegerType | FieldType.LongType => "0"
    case FieldType.FloatType | FieldType.DoubleType => "0.0"
    case FieldType.StringType => "\"\""

    case FieldType.ListType(typ) => s"Seq(${getDefault(typ, config)})"
    case FieldType.SetType(typ) => s"Seq(${getDefault(typ, config)})"
    case FieldType.MapType(k, v) =>
      val kd = getDefault(k, config)
      val vd = getDefault(v, config)
      s"Map($kd -> $vd)"

    case FieldType.EnumType(key) =>
      val e = config.getEnum(key, "field default")
      val k = e.firstVal.className
      s"${e.className}.$k.value"
    case FieldType.StructType(key) => config.getModel(key, "field default").className + "()"

    case x => throw new IllegalStateException(s"Unhandled field type [$x]")
  }
}
