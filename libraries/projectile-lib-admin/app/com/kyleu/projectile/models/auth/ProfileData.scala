package com.kyleu.projectile.models.auth

import com.kyleu.projectile.models.config.UserSettings
import com.kyleu.projectile.util.NullUtils

object ProfileData {
  def gradientColors = Seq(
    "indigo-light-blue", "light-blue-indigo", "orange-deep-orange", "deep-purple-purple", "red-pink",
    "amber-amber", "purple-pink", "teal-cyan", "blue-grey-blue-grey", "orange-amber", "indigo-blue", "brown-brown", "blue-grey-blue", "purple-deep-orange",
    "green-teal", "purple-light-blue", "cyan-cyan", "yellow-teal", "purple-deep-purple", "cyan-light-green", "purple-amber", "indigo-purple",
    "deep-purple-blue", "deep-orange-orange", "light-blue-cyan", "blue-indigo", "semi-dark"
  ).map("gradient-45deg-" + _)
  def solidColors = Seq(
    "red", "purple", "pink", "deep-purple", "cyan", "teal", "light-blue", "indigo", "blue", "light-blue", "amber darken-3", "brown darken-2", "light-green",
    "green", "lime", "yellow darken-2", "orange", "deep-orange", "blue-grey", "grey", "black"
  )
  val allColors = gradientColors ++ solidColors
}

final case class ProfileData(
    username: String = "",
    theme: String = "",

    menuColor: String = "",
    menuBackgroundColor: String = "",
    menuDark: Option[String] = None,
    menuCollapsed: Option[String] = None,
    menuSelection: String = "",

    navbarColor: String = "",
    navbarDark: Option[String] = None,

    buttonColor: String = "",
    accentColor: String = ""
) {
  private[this] def opt(s: String) = s.trim match {
    case NullUtils.str | "" => None
    case _ => Some(s)
  }

  lazy val settings = UserSettings(
    theme = theme,

    menuColor = opt(menuColor),
    menuBackgroundColor = opt(menuBackgroundColor),
    menuDark = menuDark.contains("true"),
    menuCollapsed = menuCollapsed.contains("true"),
    menuSelection = opt(menuSelection),

    navbarColor = opt(navbarColor),
    navbarDark = navbarDark.contains("true"),

    buttonColor = opt(buttonColor),
    accentColor = opt(accentColor),

    avatarUrl = None
  )
}
