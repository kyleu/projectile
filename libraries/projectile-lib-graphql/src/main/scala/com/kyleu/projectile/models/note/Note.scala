package com.kyleu.projectile.models.note

import java.time.LocalDateTime
import java.util.UUID

import com.kyleu.projectile.models.result.data.{DataField, DataFieldModel, DataSummary}
import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._

object Note {
  implicit val jsonEncoder: Encoder[Note] = deriveEncoder
  implicit val jsonDecoder: Decoder[Note] = deriveDecoder

  def empty(
    id: UUID = UUID.randomUUID, relType: Option[String] = None, relPk: Option[String] = None,
    text: String = "", author: UUID = UUID.randomUUID, created: LocalDateTime = DateUtils.now
  ) = {
    Note(id, relType, relPk, text, author, created)
  }
}

final case class Note(
    id: UUID,
    relType: Option[String],
    relPk: Option[String],
    text: String,
    author: UUID,
    created: LocalDateTime
) extends DataFieldModel {
  override def toDataFields = Seq(
    DataField("id", Some(id.toString)),
    DataField("relType", relType),
    DataField("relPk", relPk),
    DataField("text", Some(text)),
    DataField("author", Some(author.toString)),
    DataField("created", Some(created.toString))
  )

  def toSummary = DataSummary(model = "noteRow", pk = Seq(id.toString), title = s"$relType / $relPk / $text / $author / $created ($id)")
}
