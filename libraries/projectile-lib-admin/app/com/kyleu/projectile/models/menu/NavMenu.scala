package com.kyleu.projectile.models.menu

import com.kyleu.projectile.util.JsonSerializers._

object NavMenu {
  implicit val jsonEncoder: Encoder[NavMenu] = deriveEncoder
  implicit val jsonDecoder: Decoder[NavMenu] = deriveDecoder
}

case class NavMenu(
    key: String,
    title: String,
    description: Option[String] = None,
    url: Option[String] = None,
    icon: Option[String] = None,
    children: Seq[NavMenu] = Nil,
    flatSection: Boolean = false
)

