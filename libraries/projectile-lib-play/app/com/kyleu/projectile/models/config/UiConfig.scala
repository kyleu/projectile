package com.kyleu.projectile.models.config

case class UiConfig(
    projectName: String,
    menu: Seq[NavMenu] = Nil,
    urls: NavUrls = NavUrls(),
    html: NavHtml = NavHtml(),
    user: UserSettings = UserSettings.empty,
    notifications: Seq[Notification] = Nil,
    breadcrumbs: Seq[BreadcrumbEntry] = Nil
)
