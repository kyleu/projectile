package com.kyleu.projectile.components.models.config

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class Theme(
    override val value: String,
    val title: String,
    val logo: String,
    val bodyClass: String,
    val navClass: String,
    val asideClass: String,
    val contentBefore: Option[String] = None
) extends StringEnumEntry {
  override def toString = value
}

object Theme extends StringEnum[Theme] with StringCirceEnum[Theme] {
  case object Default extends Theme(
    value = "default",
    title = "Default",
    logo = "/assets/images/logo-white.svg",
    bodyClass = "page-header-light vertical-menu-nav-dark",
    navClass = "navbar-dark gradient-45deg-purple-deep-orange gradient-shadow",
    asideClass = "sidenav-light navbar-full sidenav-active-rounded"
  )

  case object Dark extends Theme(
    value = "dark",
    title = "Dark",
    logo = "/assets/images/logo-white.svg",
    bodyClass = "page-header-light vertical-dark-menu",
    navClass = "navbar-light",
    asideClass = "sidenav-dark sidenav-active-rounded"
  )

  case object Gradient extends Theme(
    value = "gradient",
    title = "Gradient",
    logo = "/assets/images/logo-white.svg",
    bodyClass = "page-header-light vertical-gradient-menu",
    navClass = "navbar-light",
    asideClass = "sidenav-dark gradient-45deg-deep-purple-blue sidenav-gradient sidenav-active-rounded"
  )

  case object Modern extends Theme(
    value = "modern",
    title = "Modern",
    logo = "/assets/images/logo-black.svg",
    bodyClass = "page-header-dark vertical-modern-menu",
    navClass = "navbar-dark gradient-45deg-indigo-purple no-shadow",
    asideClass = "sidenav-light sidenav-active-square",
    contentBefore = Some("gradient-45deg-indigo-purple")
  )

  override val values = findValues
}

