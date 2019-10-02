package com.kyleu.projectile.models.feedback

import com.kyleu.projectile.models.result.data.{DataField, DataFieldModel, DataSummary}
import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._
import java.time.LocalDateTime
import java.util.UUID

object Feedback {
  implicit val jsonEncoder: Encoder[Feedback] = (r: Feedback) => io.circe.Json.obj(
    ("id", r.id.asJson),
    ("text", r.text.asJson),
    ("authorId", r.authorId.asJson),
    ("authorEmail", r.authorEmail.asJson),
    ("created", r.created.asJson),
    ("status", r.status.asJson)
  )

  implicit val jsonDecoder: Decoder[Feedback] = (c: io.circe.HCursor) => for {
    id <- c.downField("id").as[UUID]
    text <- c.downField("text").as[String]
    authorId <- c.downField("authorId").as[UUID]
    authorEmail <- c.downField("authorEmail").as[String]
    created <- c.downField("created").as[LocalDateTime]
    status <- c.downField("status").as[String]
  } yield Feedback(id, text, authorId, authorEmail, created, status)

  def empty(
    id: UUID = UUID.randomUUID,
    text: String = "",
    authorId: UUID = UUID.randomUUID,
    authorEmail: String = "",
    created: LocalDateTime = DateUtils.now,
    status: String = ""
  ) = {
    Feedback(id, text, authorId, authorEmail, created, status)
  }
}

final case class Feedback(
    id: UUID,
    text: String,
    authorId: UUID,
    authorEmail: String,
    created: LocalDateTime,
    status: String
) extends DataFieldModel {
  override def toDataFields = Seq(
    DataField("id", Some(id.toString)),
    DataField("text", Some(text)),
    DataField("authorId", Some(authorId.toString)),
    DataField("authorEmail", Some(authorEmail)),
    DataField("created", Some(created.toString)),
    DataField("status", Some(status))
  )

  def toSummary = DataSummary(model = "feedback", pk = id.toString, entries = Map("AuthorId" -> Some(authorId.toString), "Created" -> Some(created.toString)))
}
