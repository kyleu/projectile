package com.kyleu.projectile.models.error

import java.time.LocalDateTime
import java.util.UUID

import com.kyleu.projectile.models.result.data.{DataField, DataFieldModel, DataSummary}
import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._

object SystemError {
  implicit val jsonEncoder: Encoder[SystemError] = (r: SystemError) => io.circe.Json.obj(
    ("id", r.id.asJson),
    ("context", r.context.asJson),
    ("userId", r.userId.asJson),
    ("cls", r.cls.asJson),
    ("message", r.message.asJson),
    ("stacktrace", r.stacktrace.asJson),
    ("occurred", r.occurred.asJson)
  )

  implicit val jsonDecoder: Decoder[SystemError] = (c: io.circe.HCursor) => for {
    id <- c.downField("id").as[UUID]
    context <- c.downField("context").as[String]
    userId <- c.downField("userId").as[Option[UUID]]
    cls <- c.downField("cls").as[String]
    message <- c.downField("message").as[String]
    stacktrace <- c.downField("stacktrace").as[Option[String]]
    occurred <- c.downField("occurred").as[LocalDateTime]
  } yield SystemError(id, context, userId, cls, message, stacktrace, occurred)

  def empty(
    id: UUID = UUID.randomUUID,
    context: String = "",
    userId: Option[UUID] = None,
    cls: String = "",
    message: String = "",
    stacktrace: Option[String] = None,
    occurred: LocalDateTime = DateUtils.now
  ) = {
    SystemError(id, context, userId, cls, message, stacktrace, occurred)
  }
}

final case class SystemError(
    id: UUID,
    context: String,
    userId: Option[UUID],
    cls: String,
    message: String,
    stacktrace: Option[String],
    occurred: LocalDateTime
) extends DataFieldModel {
  override def toDataFields = Seq(
    DataField("id", Some(id.toString)),
    DataField("context", Some(context)),
    DataField("userId", userId.map(_.toString)),
    DataField("cls", Some(cls)),
    DataField("message", Some(message)),
    DataField("stacktrace", stacktrace),
    DataField("occurred", Some(occurred.toString))
  )

  def toSummary = DataSummary(model = "systemError", pk = id.toString, title = s"id: $id, context: $context, userId: ${userId.map(_.toString).getOrElse("∅")}, cls: $cls, message: $message, occurred: $occurred")
}
