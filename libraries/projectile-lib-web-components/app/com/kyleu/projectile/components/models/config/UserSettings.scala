package com.kyleu.projectile.components.models.config

case class UserSettings(
    name: String = "Guest",
    theme: UiTheme = UiTheme.default,
    avatarUrl: Option[String] = None
)
