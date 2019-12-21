package com.kyleu.projectile.models.database.schema

import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.util.JsonSerializers._

object Column {
  implicit val jsonEncoder: Encoder[Column] = deriveEncoder
  implicit val jsonDecoder: Decoder[Column] = deriveDecoder
}

final case class Column(
    name: String,
    description: Option[String] = None,
    definition: Option[String] = None,
    notNull: Boolean = false,
    autoIncrement: Boolean = false,
    columnType: FieldType = FieldType.StringType,
    sqlTypeCode: Int = 12,
    sqlTypeName: String = "varchar",
    size: String = Int.MaxValue.toString,
    sizeAsInt: Int = Int.MaxValue,
    scale: Int = 0,
    defaultValue: Option[String] = None
)
