package com.kyleu.projectile.models.database

import java.time.{ZoneOffset, ZonedDateTime}
import java.util.UUID

import com.kyleu.projectile.models.tag.Tag
import com.kyleu.projectile.util.{DateUtils, EncryptionUtils, JsonSerializers}
import enumeratum.values.{StringEnum, StringEnumEntry}
import enumeratum.{CirceEnum, Enum, EnumEntry}
import org.postgresql.jdbc.PgArray
import org.postgresql.util.PGobject

import scala.reflect.ClassTag

sealed abstract class DatabaseFieldType[T](val key: String, val isNumeric: Boolean = false, val isList: Boolean = false) extends EnumEntry {
  def fromString(s: String): T = throw new NotImplementedError()
  def coerce(x: Any): T = x.asInstanceOf[T]
  def apply(row: Row, col: String) = coerce(row.as[Any](col))
  def opt(row: Row, col: String) = row.asOpt[Any](col).map(coerce)
}

object DatabaseFieldType extends Enum[DatabaseFieldType[_]] with CirceEnum[DatabaseFieldType[_]] with DatabaseFieldHelper {
  case object StringType extends DatabaseFieldType[String]("string") {
    override def fromString(s: String) = s
    override def coerce(x: Any) = stringCoerce(x)
  }

  case object EncryptedStringType extends DatabaseFieldType[String]("encrypted") {
    override def fromString(s: String) = s
    override def coerce(x: Any) = EncryptionUtils.decrypt(x.toString)
  }

  case object BooleanType extends DatabaseFieldType[Boolean]("boolean") {
    override def fromString(s: String) = s == "true"
    override def coerce(x: Any) = boolCoerce(x)
  }
  case object ByteType extends DatabaseFieldType[Byte]("byte") {
    override def fromString(s: String) = s.toByte
    override def coerce(x: Any) = byteCoerce(x)
  }
  case object ShortType extends DatabaseFieldType[Short]("short", isNumeric = true) {
    override def fromString(s: String) = s.toShort
  }
  case object IntegerType extends DatabaseFieldType[Int]("int", isNumeric = true) {
    override def fromString(s: String) = s.toInt
  }
  case object LongType extends DatabaseFieldType[Long]("long", isNumeric = true) {
    override def fromString(s: String) = s.toLong
    override def coerce(x: Any) = longCoerce(x)
  }
  case object FloatType extends DatabaseFieldType[Float]("float", isNumeric = true) {
    override def fromString(s: String) = s.toFloat
  }
  case object DoubleType extends DatabaseFieldType[Double]("double", isNumeric = true) {
    override def fromString(s: String) = s.toDouble
  }
  case object BigDecimalType extends DatabaseFieldType[BigDecimal]("decimal", isNumeric = true) {
    override def fromString(s: String) = BigDecimal(s)
    override def coerce(x: Any) = bigDecimalCoerce(x)
  }

  case object DateType extends DatabaseFieldType[java.time.LocalDate]("date") {
    override def fromString(s: String) = DateUtils.fromDateString(s)
    override def coerce(x: Any) = x.asInstanceOf[java.sql.Date].toLocalDate
  }
  case object TimeType extends DatabaseFieldType[java.time.LocalTime]("time") {
    override def fromString(s: String) = DateUtils.fromTimeString(s)
    override def coerce(x: Any) = x.asInstanceOf[java.sql.Time].toLocalTime
  }
  case object TimestampType extends DatabaseFieldType[java.time.LocalDateTime]("timestamp") {
    override def fromString(s: String) = DateUtils.fromIsoString(s)
    override def coerce(x: Any) = x.asInstanceOf[java.sql.Timestamp].toLocalDateTime
  }
  case object TimestampZonedType extends DatabaseFieldType[java.time.ZonedDateTime]("timestamp") {
    override def fromString(s: String) = DateUtils.fromIsoStringZoned(s)
    override def coerce(x: Any) = ZonedDateTime.ofInstant(x.asInstanceOf[java.sql.Timestamp].toInstant, ZoneOffset.UTC)
  }

  case object RefType extends DatabaseFieldType[String]("ref") {
    override def fromString(s: String) = s
  }
  case object XmlType extends DatabaseFieldType[String]("xml") {
    override def fromString(s: String) = s
  }
  case object UuidType extends DatabaseFieldType[java.util.UUID]("uuid") {
    override def fromString(s: String) = UUID.fromString(s)
    override def coerce(x: Any) = uuidCoerce(x)
  }

  final case class EnumType[T <: StringEnumEntry](t: StringEnum[T]) extends DatabaseFieldType[T]("enum") {
    override def coerce(x: Any) = t.withValue(x.toString)
  }

  case object ObjectType extends DatabaseFieldType[String]("object") {
    override def fromString(s: String) = s
  }
  case object StructType extends DatabaseFieldType[String]("struct") {
    override def fromString(s: String) = s
  }
  case object JsonType extends DatabaseFieldType[io.circe.Json]("json") {
    override def fromString(s: String) = JsonSerializers.parseJson(s).getOrElse(throw new IllegalStateException("Invalid JSON"))
    override def coerce(x: Any) = JsonSerializers.parseJson(x.asInstanceOf[PGobject].getValue).getOrElse(throw new IllegalStateException("Invalid JSON"))
  }

  case object CodeType extends DatabaseFieldType[String]("code") {
    override def fromString(s: String) = s
  }
  case object TagsType extends DatabaseFieldType[List[Tag]]("tags") {
    override def fromString(s: String) = Tag.fromString(s)
    override def coerce(x: Any) = tagsCoerce(x)
  }

  case object ByteArrayType extends DatabaseFieldType[Array[Byte]]("byteArray") {
    override def fromString(s: String) = s.getBytes
    override def coerce(x: Any) = binaryCoerce(x)
  }
  case object IntArrayType extends DatabaseFieldType[List[Int]]("intArray", isList = true) {
    override def coerce(x: Any) = x.asInstanceOf[PgArray].getArray.asInstanceOf[Array[Any]].flatMap(x => Option.apply(x).map(intCoerce)).toList
  }
  case object LongArrayType extends DatabaseFieldType[List[Long]]("longArray", isList = true) {
    override def coerce(x: Any) = x.asInstanceOf[PgArray].getArray.asInstanceOf[Array[Long]].flatMap(x => Option.apply(x)).toList
  }
  final case class EnumArrayType[T <: StringEnumEntry](t: StringEnum[T])(implicit tag: ClassTag[T]) extends DatabaseFieldType[List[T]]("enumArray", isList = true) {
    override def coerce(x: Any) = x.asInstanceOf[PgArray].getArray.asInstanceOf[Array[Any]].flatMap(x => Option(x).map(_.toString)).map(t.withValue).toList
  }
  case object StringArrayType extends DatabaseFieldType[List[String]]("stringArray", isList = true) {
    override def coerce(x: Any) = x.asInstanceOf[PgArray].getArray.asInstanceOf[Array[Any]].map(s => Option(s).map(_.toString)).toList.flatten
  }
  case object UuidArrayType extends DatabaseFieldType[List[java.util.UUID]]("uuidArray", isList = true) {
    override def coerce(x: Any) = x.asInstanceOf[PgArray].getArray.asInstanceOf[Array[java.util.UUID]].toList.map(Option.apply).flatten
  }

  case object UnknownType extends DatabaseFieldType[String]("unknown") {
    override def fromString(s: String) = s
  }

  override val values = findValues
}
