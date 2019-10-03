package com.kyleu.projectile.models.feature.openapi

import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.export.{ExportEnum, ExportField}
import com.kyleu.projectile.models.output.file.JsonFile

object OpenApiPropertyHelper {
  def contentFor(t: FieldType, file: JsonFile, enums: Seq[ExportEnum]): Unit = t match {
    case FieldType.IntegerType | FieldType.LongType | FieldType.SerialType => file.add("\"type\": \"integer\"")
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
    case FieldType.EnumType(key) =>
      val e = enums.find(_.key == key).getOrElse(throw new IllegalStateException(s"Cannot file enum [$key]"))
      file.add("\"type\": \"" + e.className + "\"")
    case FieldType.ListType(typ) =>
      file.add("\"type\": \"array\",")
      file.add("\"items\": {", 1)
      contentFor(typ, file, enums)
      file.add("}", -1)
    case FieldType.SetType(typ) =>
      file.add("\"type\": \"array\",")
      file.add("\"items\": {", 1)
      contentFor(typ, file, enums)
      file.add("}", -1)
    case FieldType.StringType | FieldType.EncryptedStringType => file.add("\"type\": \"string\"")
    case _ => file.add("\"type\": \"string\"")
  }

  def propertyFor(f: ExportField, file: JsonFile, last: Boolean, enums: Seq[ExportEnum]) = {
    val comma = if (last) { "" } else { "," }
    file.add("\"" + f.propertyName + "\": {", 1)
    contentFor(f.t, file, enums)
    file.add("}" + comma, -1)
  }
}
