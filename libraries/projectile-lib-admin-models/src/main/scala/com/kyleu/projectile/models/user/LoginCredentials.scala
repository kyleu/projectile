package com.kyleu.projectile.models.user

import com.kyleu.projectile.util.JsonSerializers._

object LoginCredentials {
  implicit val jsonEncoder: Encoder[LoginCredentials] = deriveEncoder
  implicit val jsonDecoder: Decoder[LoginCredentials] = deriveDecoder
}

case class LoginCredentials(providerID: String, providerKey: String)
