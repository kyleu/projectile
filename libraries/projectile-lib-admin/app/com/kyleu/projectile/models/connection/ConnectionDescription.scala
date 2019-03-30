package com.kyleu.projectile.models.connection

import java.time.LocalDateTime
import java.util.UUID

import com.kyleu.projectile.util.JsonSerializers._

object ConnectionDescription {
  implicit val jsonEncoder: Encoder[ConnectionDescription] = deriveEncoder
  implicit val jsonDecoder: Decoder[ConnectionDescription] = deriveDecoder
}

final case class ConnectionDescription(id: UUID, userId: String, username: String, channel: String, started: LocalDateTime)
