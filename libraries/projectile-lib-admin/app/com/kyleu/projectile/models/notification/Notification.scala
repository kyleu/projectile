package com.kyleu.projectile.models.notification

import java.time.LocalDateTime

case class Notification(
    title: String,
    url: String,
    icon: String,
    occurred: LocalDateTime
)
