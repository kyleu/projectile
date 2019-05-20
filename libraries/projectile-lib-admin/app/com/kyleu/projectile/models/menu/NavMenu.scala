package com.kyleu.projectile.models.menu

case class NavMenu(
    key: String,
    title: String,
    url: Option[String] = None,
    icon: Option[String] = None,
    children: Seq[NavMenu] = Nil,
    flatSection: Boolean = false
)

