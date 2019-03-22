package com.kyleu.projectile.components.models.config

object NavMenu {
  val test = Seq(
    NavMenu(key = "home", url = Some("/"), title = "Home", icon = Some("bookmark_border")),
    NavMenu(key = "section1", title = "Section 1"),
    NavMenu(key = "test", title = "Tests", icon = Some("settings"), children = Seq(
      NavMenu(key = "a", url = Some("/"), title = "Section A", icon = Some("bookmark_border")),
      NavMenu(key = "b", url = Some("/"), title = "Section B", icon = Some("bookmark_border")),
      NavMenu(key = "c", url = Some("/"), title = "Section C", icon = Some("bookmark_border")),
      NavMenu(key = "d", url = Some("/"), title = "Section D", icon = Some("bookmark_border"))
    )),
    NavMenu(key = "about", url = Some("/"), title = "About", icon = Some("info"))
  )
}

case class NavMenu(key: String, title: String, url: Option[String] = None, icon: Option[String] = None, children: Seq[NavMenu] = Nil)

