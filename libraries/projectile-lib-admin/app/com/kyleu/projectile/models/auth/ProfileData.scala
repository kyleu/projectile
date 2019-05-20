package com.kyleu.projectile.models.auth

import com.kyleu.projectile.models.config.UserSettings
import com.kyleu.projectile.util.NullUtils

object ProfileData {
  def gradientColors = Seq(
    "indigo-blue", "purple-deep-orange", "light-blue-cyan", "purple-amber", "purple-deep-purple",
    "deep-orange-orange", "green-teal", "indigo-light-blue", "red-pink"
  ).map("gradient-45deg-" + _)
  def solidColors = Seq("red", "purple", "pink", "deep-purple", "cyan", "teal", "light-blue", "amber darken-3", "brown darken-2")
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
    navbarDark: Option[String] = None
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

    avatarUrl = None
  )
}
