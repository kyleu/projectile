package com.kyleu.projectile.models.config

case class NavUrls(
    home: String = "/",
    search: String = "/s",
    profile: String = "/profile",
    signup: String = "/profile/signup",
    changePassword: String = "/profile/password",
    signin: String = "/profile/signin",
    signout: String = "/profile/signout",
    oauth: Option[String] = None
)
