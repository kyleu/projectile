package com.kyleu.projectile.models.config

import com.kyleu.projectile.models.menu.NavMenu
import com.kyleu.projectile.models.notification.Notification

case class UiConfig(
    projectName: String,
    menu: Seq[NavMenu] = Nil,
    urls: NavUrls = NavUrls(),
    html: NavHtml = NavHtml(),
    user: UserSettings = UserSettings.empty,
    notifications: Seq[Notification] = Nil,
    breadcrumbs: Seq[BreadcrumbEntry] = Nil
)
