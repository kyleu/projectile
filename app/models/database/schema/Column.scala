package models.database.schema

import util.JsonSerializers._

object Column {
  implicit val jsonEncoder: Encoder[Column] = deriveEncoder
  implicit val jsonDecoder: Decoder[Column] = deriveDecoder
}

case class Column(
    name: String,
    description: Option[String] = None,
    definition: Option[String] = None,
    primaryKey: Boolean = false,
    notNull: Boolean = false,
    autoIncrement: Boolean = false,
    columnType: ColumnType = ColumnType.StringType,
    sqlTypeCode: Int = 12,
    sqlTypeName: String = "varchar",
    size: String = Int.MaxValue.toString,
    sizeAsInt: Int = Int.MaxValue,
    scale: Int = 0,
    defaultValue: Option[String] = None
)
