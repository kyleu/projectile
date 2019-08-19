package com.kyleu.projectile.models.config

import com.kyleu.projectile.util.JsonSerializers._

object UserSettings {
  implicit val jsonEncoder: Encoder[UserSettings] = deriveEncoder
  implicit val jsonDecoder: Decoder[UserSettings] = deriveDecoder

  val empty = UserSettings()
}

case class UserSettings(
    theme: String = "default",

    menuColor: Option[String] = Some("gradient-45deg-indigo-blue"),
    menuBackgroundColor: Option[String] = None,
    menuDark: Boolean = true,
    menuCollapsed: Boolean = false,
    menuSelection: Option[String] = None,

    navbarColor: Option[String] = Some("gradient-45deg-indigo-blue"),
    navbarDark: Boolean = true,

    buttonColor: Option[String] = None,
    accentColor: Option[String] = None,

    avatarUrl: Option[String] = None
) {
  val menuDarkClass = if (menuDark) { "sidenav-dark" } else { "sidenav-light" }

  val logo = if (menuDark) { "/assets/images/logo-white.svg" } else { "/assets/images/logo-black.svg" }
  val navbarDarkClass = if (navbarDark) { "navbar-dark" } else { "navbar-light" }

  val textColor = accentColor.map(_ + "-text").getOrElse("")
}
