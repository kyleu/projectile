package com.kyleu.projectile.models.config

import play.twirl.api.Html

case class NavContent(
    head: Option[Html] = None,
    header: Option[Html] = None,
    footer: Option[Html] = None
)
