package com.projectile.models.feature.openapi

import com.projectile.models.export.FieldType
import com.projectile.models.export.{ExportEnum, ExportField}
import com.projectile.models.output.file.JsonFile

object OpenApiPropertyHelper {
  def contentFor(t: FieldType, nativeType: String, file: JsonFile, enums: Seq[ExportEnum]) = t match {
    case FieldType.IntegerType | FieldType.LongType => file.add("\"type\": \"integer\"")
    case FieldType.BigDecimalType | FieldType.DoubleType | FieldType.FloatType => file.add("\"type\": \"number\"")
    case FieldType.UuidType =>
      file.add("\"type\": \"string\",")
      file.add("\"example\": \"00000000-0000-0000-0000-000000000000\"")
    case FieldType.BooleanType => file.add("\"type\": \"boolean\"")
    case FieldType.TimestampType =>
      file.add("\"type\": \"string\",")
      file.add("\"example\": \"2018-01-01 00:00:00\"")
    case FieldType.TimestampZonedType =>
      file.add("\"type\": \"string\",")
      file.add("\"example\": \"2018-01-01 00:00:00+0\"")
    case FieldType.TimeType =>
      file.add("\"type\": \"string\",")
      file.add("\"example\": \"00:00:00\"")
    case FieldType.DateType =>
      file.add("\"type\": \"string\",")
      file.add("\"example\": \"2018-01-01\"")
    case FieldType.TagsType | FieldType.JsonType => file.add("\"type\": \"object\"")
    case FieldType.EnumType =>
      // val e = enums.find(_.name == nativeType).getOrElse(throw new IllegalStateException(s"Cannot file enum [$nativeType]."))
      file.add("\"type\": \"string\"")
    case FieldType.ArrayType =>
      file.add("\"type\": \"array\",")
      nativeType match {
        case x =>
          file.add("\"items\": {", 1)
          file.add("\"type\": \"" + x + "\"")
          file.add("}", -1)
      }
    case FieldType.StringType | FieldType.EncryptedStringType | FieldType.UnknownType => file.add("\"type\": \"string\"")
    case FieldType.ByteArrayType => file.add("\"type\": \"string\"")
    case x => throw new IllegalStateException(s"Unhandled openapi property for type [$x].")
  }

  def propertyFor(f: ExportField, file: JsonFile, last: Boolean, enums: Seq[ExportEnum]) = {
    val comma = if (last) { "" } else { "," }
    file.add("\"" + f.propertyName + "\": {", 1)
    contentFor(f.t, f.nativeType, file, enums)
    file.add("}" + comma, -1)
  }
}
