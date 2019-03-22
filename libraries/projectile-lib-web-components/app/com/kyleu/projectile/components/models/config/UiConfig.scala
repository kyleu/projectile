package com.kyleu.projectile.components.models.config

import com.kyleu.projectile.components.controllers.Assets

case class UiConfig(
    projectName: String,
    menu: Seq[NavMenu],
    urls: NavUrls,
    html: NavHtml,
    user: UserSettings,
    breadcrumbs: Seq[String]
) {

  def asset(p: String) = Assets.path(p)
}
