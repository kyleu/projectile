package com.kyleu.projectile.models.supervisor

import java.time.LocalDateTime
import java.util.UUID

import com.kyleu.projectile.util.JsonSerializers._

object SessionDescription {
  implicit val jsonEncoder: Encoder[SessionDescription] = deriveEncoder
  implicit val jsonDecoder: Decoder[SessionDescription] = deriveDecoder
}

final case class SessionDescription(id: UUID, channel: String, started: LocalDateTime)

