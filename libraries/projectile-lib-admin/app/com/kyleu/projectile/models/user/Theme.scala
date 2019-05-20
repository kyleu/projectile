package com.kyleu.projectile.models.user

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class Theme(
    override val value: String,
    val title: String,
    val bodyClass: String,
    val asideClass: String = "",
    val contentBefore: Option[String] = None
) extends StringEnumEntry {
  override def toString = value
}

object Theme extends StringEnum[Theme] with StringCirceEnum[Theme] {
  case object Default extends Theme(
    value = "default",
    title = "Default",
    bodyClass = "page-header-light vertical-menu-nav-dark",
    asideClass = "navbar-full"
  )

  case object Dark extends Theme(
    value = "dark",
    title = "Dark",
    bodyClass = "page-header-light vertical-dark-menu",
  )

  case object Gradient extends Theme(
    value = "gradient",
    title = "Gradient",
    bodyClass = "page-header-light vertical-gradient-menu",
  )

  case object Modern extends Theme(
    value = "modern",
    title = "Modern",
    bodyClass = "page-header-dark vertical-modern-menu",
    contentBefore = Some("")
  )

  override val values = findValues
}

