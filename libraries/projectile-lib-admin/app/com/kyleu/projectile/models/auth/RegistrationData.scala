package com.kyleu.projectile.models.auth

final case class RegistrationData(
    username: String = "",
    email: String = "",
    password: String = ""
)
