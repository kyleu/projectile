package com.kyleu.projectile.models.config

import com.kyleu.projectile.util.metrics.MetricsConfig
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticatorSettings
import com.mohiva.play.silhouette.impl.providers.OAuth2Settings
import play.api.{Environment, Mode}

@javax.inject.Singleton
class Configuration @javax.inject.Inject() (val cnf: play.api.Configuration, val metrics: MetricsConfig, env: Environment) {
  val projectName = cnf.get[String]("projectName")
  val debug = env.mode == Mode.Dev
  val secretKey = cnf.get[String]("play.http.secret.key")
  val scheduledTaskEnabled = cnf.get[Boolean]("scheduled.task.enabled")

  val authCookieSettings = {
    import scala.concurrent.duration._
    val cfg = cnf.get[play.api.Configuration]("silhouette.authenticator.cookie")

    CookieAuthenticatorSettings(
      cookieName = cfg.get[String]("name"),
      cookiePath = cfg.get[String]("path"),
      secureCookie = cfg.get[Boolean]("secure"),
      httpOnlyCookie = true,
      useFingerprinting = cfg.get[Boolean]("useFingerprinting"),
      cookieMaxAge = Some(cfg.get[Int]("maxAge").seconds),
      authenticatorIdleTimeout = Some(cfg.get[Int]("idleTimeout").seconds),
      authenticatorExpiry = cfg.get[Int]("expiry").seconds
    )
  }

  val (authWhitelistDomain, authMicrosoftSettings) = {
    val cfg = cnf.get[play.api.Configuration]("silhouette.authenticator.microsoft")

    val whitelist = Some(cfg.get[String]("whitelistDomain")).map(_.trim).filter(_.nonEmpty)

    whitelist -> OAuth2Settings(
      authorizationURL = Some(cfg.get[String]("authorization")),
      accessTokenURL = cfg.get[String]("accessToken"),
      redirectURL = Some(cfg.get[String]("redirect")),
      apiURL = None,
      clientID = cfg.get[String]("clientId"),
      clientSecret = cfg.get[String]("clientSecret"),
      scope = Some(cfg.get[String]("scope")),
      authorizationParams = Map.empty,
      accessTokenParams = Map.empty,
      customProperties = Map.empty
    )
  }
}
