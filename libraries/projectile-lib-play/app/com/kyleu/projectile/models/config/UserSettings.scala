package com.kyleu.projectile.models.config

import com.kyleu.projectile.util.JsonSerializers._

object UserSettings {
  implicit val jsonEncoder: Encoder[UserSettings] = deriveEncoder
  implicit val jsonDecoder: Decoder[UserSettings] = deriveDecoder

  val empty = UserSettings("Guest", "default", None)
}

case class UserSettings(
    name: String,
    theme: String,
    avatarUrl: Option[String] = None
)
