package com.kyleu.projectile.components.models.config

case class NavUrls(
    home: String = "/",
    search: String = "/s",
    signup: String = "/profile/signup",
    signin: String = "/profile/signin",
    signout: String = "/profile/signout",
    oauth: Option[String] = None
)
