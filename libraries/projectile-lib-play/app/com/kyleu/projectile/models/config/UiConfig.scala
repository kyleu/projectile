package com.kyleu.projectile.models.config

object UiConfig {
  val empty = UiConfig(projectName = "---", menu = Nil, urls = NavUrls(), html = NavHtml(play.twirl.api.Html("")), user = UserSettings.empty, breadcrumbs = Nil)
}

case class UiConfig(
    projectName: String,
    menu: Seq[NavMenu],
    urls: NavUrls,
    html: NavHtml,
    user: UserSettings,
    breadcrumbs: Seq[BreadcrumbEntry]
)
