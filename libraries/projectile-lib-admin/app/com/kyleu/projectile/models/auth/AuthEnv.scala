package com.kyleu.projectile.models.auth

import com.kyleu.projectile.models.user.SystemUser
import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator

class AuthEnv() extends Env {
  type I = SystemUser
  type A = CookieAuthenticator
}
