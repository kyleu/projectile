package com.kyleu.projectile.components.models.config

import com.kyleu.projectile.components.controllers.Assets

object UiTheme {
  val default = UiTheme(
    key = "default",
    logo = "/assets/images/logo-white.svg",
    bodyClass = "page-header-light vertical-menu-nav-dark",
    navClass = "navbar-dark gradient-45deg-purple-deep-orange gradient-shadow",
    asideClass = "sidenav-light navbar-full sidenav-active-rounded"
  )

  val dark = UiTheme(
    key = "dark",
    logo = "/assets/images/logo-white.svg",
    bodyClass = "page-header-light vertical-dark-menu",
    navClass = "navbar-light",
    asideClass = "sidenav-dark sidenav-active-rounded"
  )

  val gradient = UiTheme(
    key = "gradient",
    logo = "/assets/images/logo-white.svg",
    bodyClass = "page-header-light vertical-gradient-menu",
    navClass = "navbar-light",
    asideClass = "sidenav-dark gradient-45deg-deep-purple-blue sidenav-gradient sidenav-active-rounded"
  )

  val modern = UiTheme(
    key = "modern",
    logo = "/assets/images/logo-black.svg",
    bodyClass = "page-header-dark vertical-modern-menu",
    navClass = "navbar-dark gradient-45deg-indigo-purple no-shadow",
    asideClass = "sidenav-light sidenav-active-square",
    contentBefore = Some("gradient-45deg-indigo-purple")
  )

  val all = Seq(default, dark, gradient, modern)

  def withValue(key: String) = all.find(_.key == key).getOrElse(throw new IllegalStateException(s"No theme with key [$key] is available"))
}

case class UiTheme(key: String, logo: String, bodyClass: String, navClass: String, asideClass: String, contentBefore: Option[String] = None)
