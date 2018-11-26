package com.projectile.models.export

import enumeratum.values._

sealed abstract class FieldType(
    override val value: String,
    val asScala: String,
    val fromString: String,
    val requiredImport: Option[String] = None,
    val isNumeric: Boolean = false
) extends StringEnumEntry {

  val asScalaFull = requiredImport match {
    case Some(pkg) => pkg + "." + asScala
    case None => asScala
  }

  val className = getClass.getSimpleName.stripSuffix("$")

  override def toString = value
}

object FieldType extends StringEnum[FieldType] with StringCirceEnum[FieldType] {
  case object StringType extends FieldType("string", "String", "xxx")
  case object EncryptedStringType extends FieldType("encrypted", "String", "xxx")

  case object BooleanType extends FieldType("boolean", "Boolean", "xxx == \"true\"")
  case object ByteType extends FieldType("byte", "Byte", "xxx.toInt.toByte")
  case object ShortType extends FieldType("short", "Short", "xxx.toInt.toShort", isNumeric = true)
  case object IntegerType extends FieldType("integer", "Int", "xxx.toInt", isNumeric = true)
  case object LongType extends FieldType("long", "Long", "xxx.toLong", isNumeric = true)
  case object FloatType extends FieldType("float", "Float", "xxx.toFloat", isNumeric = true)
  case object DoubleType extends FieldType("double", "Double", "xxx.toDouble", isNumeric = true)
  case object BigDecimalType extends FieldType("decimal", "BigDecimal", "BigDecimal(xxx)", isNumeric = true)

  case object DateType extends FieldType("date", "LocalDate", "DateUtils.fromDateString(xxx)", requiredImport = Some("java.time"))
  case object TimeType extends FieldType("time", "LocalTime", "DateUtils.fromTimeString(xxx)", requiredImport = Some("java.time"))
  case object TimestampType extends FieldType("timestamp", "LocalDateTime", "DateUtils.fromIsoString(xxx)", requiredImport = Some("java.time"))
  case object TimestampZonedType extends FieldType("timestamptz", "ZonedDateTime", "DateUtils.fromIsoStringZoned(xxx)", requiredImport = Some("java.time"))

  case object RefType extends FieldType("ref", "String", "xxx")
  case object XmlType extends FieldType("xml", "String", "xxx")
  case object UuidType extends FieldType("uuid", "UUID", "UUID.fromString(xxx)", requiredImport = Some("java.util"))

  case object ObjectType extends FieldType("object", "String", "xxx")
  case object StructType extends FieldType("struct", "String", "xxx")
  case object JsonType extends FieldType("json", "Json", "util.JsonSerializers.toJson(xxx)", requiredImport = Some("io.circe"))

  case object EnumType extends FieldType("enum", "String", "xxx")
  case object CodeType extends FieldType("code", "String", "xxx")
  case object TagsType extends FieldType("hstore", "List[Tag]", "Tag.seqFromString(xxx)")

  case object ByteArrayType extends FieldType("byteArray", "Array[Byte]", "xxx.split(\",\").map(_.toInt.toByte)")
  case object ArrayType extends FieldType("array", "Array[Any]", "xxx.split(\",\")") {
    def valForSqlType(s: String) = s match {
      case _ if s.startsWith("_int") => "List[Long]"
      case _ if s.startsWith("_uuid") => "List[UUID]"
      case _ => "List[String]"
    }
    def typForSqlType(s: String) = s match {
      case _ if s.startsWith("_int4") => "IntArrayType"
      case _ if s.startsWith("_int") => "LongArrayType"
      case _ if s.startsWith("_uuid") => "UuidArrayType"
      case _ => "StringArrayType"
    }
  }

  case object ComplexType extends FieldType("complex", "COMPLEX", "COMPLEX(xxx)")

  case object UnknownType extends FieldType("unknown", "Any", "xxx")

  override def values = findValues
}
