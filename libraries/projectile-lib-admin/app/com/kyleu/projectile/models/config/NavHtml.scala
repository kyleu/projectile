package com.kyleu.projectile.models.config

import play.twirl.api.Html

case class NavHtml(
    menu: Html = Html("Guest"),
    additionalScripts: Seq[String] = Nil,
    additionalStylesheets: Seq[String] = Nil
)
