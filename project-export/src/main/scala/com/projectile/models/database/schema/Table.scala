package com.projectile.models.database.schema

import com.projectile.util.JsonSerializers._

object Table {
  implicit val jsonEncoder: Encoder[Table] = deriveEncoder
  implicit val jsonDecoder: Decoder[Table] = deriveDecoder
}

case class Table(
    name: String,
    catalog: Option[String],
    schema: Option[String],
    description: Option[String],
    definition: Option[String],

    columns: Seq[Column] = Nil,
    rowIdentifier: Seq[String] = Nil,
    primaryKey: Option[PrimaryKey] = None,
    foreignKeys: Seq[ForeignKey] = Nil,
    indexes: Seq[Index] = Nil
)
