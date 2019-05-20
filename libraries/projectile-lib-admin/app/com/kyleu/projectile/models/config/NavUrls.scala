package com.kyleu.projectile.models.config

case class NavUrls(
    home: String = "/",

    search: String = "/admin/search",

    profile: String = "/profile",
    signup: String = "/profile/signup",
    signupAllowed: Boolean = true,
    changePassword: String = "/profile/password",
    signin: String = "/profile/signin",
    signout: String = "/profile/signout",
    oauth: String = "/profile/signin/",
    oauthProviders: Seq[String] = Nil
)
