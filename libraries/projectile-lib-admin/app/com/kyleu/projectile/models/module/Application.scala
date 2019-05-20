package com.kyleu.projectile.models.module

import java.util.TimeZone

import akka.actor.ActorSystem
import com.google.inject.Injector
import com.kyleu.projectile.models.auth.AuthEnv
import com.kyleu.projectile.models.config.{Configuration, UiConfig}
import com.kyleu.projectile.models.database.DatabaseConfig
import com.kyleu.projectile.models.notification.Notification
import com.kyleu.projectile.models.status.StatusProvider
import com.kyleu.projectile.models.user.{Role, SystemUser}
import com.kyleu.projectile.models.web.TracingWSClient
import com.kyleu.projectile.services.cache.CacheService
import com.kyleu.projectile.services.database._
import com.kyleu.projectile.services.notification.NotificationService
import com.kyleu.projectile.util.metrics.Instrumented
import com.kyleu.projectile.util.tracing.TracingService
import com.kyleu.projectile.util.{EncryptionUtils, Logging}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.inject.ApplicationLifecycle

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.control.NonFatal

object Application {
  abstract class UiConfigProvider() {
    def configForUser(su: Option[SystemUser], admin: Boolean, notifications: Seq[Notification], breadcrumbs: String*): UiConfig
    def allowRegistration: Boolean = true
    def defaultRole: Role = Role.Admin
  }
}

@javax.inject.Singleton
class Application @javax.inject.Inject() (
    val config: Configuration,
    val lifecycle: ApplicationLifecycle,
    val actorSystem: ActorSystem,
    val silhouette: Silhouette[AuthEnv],
    val ws: TracingWSClient,
    val tracing: TracingService,
    val db: JdbcDatabase,
    statusProvider: StatusProvider,
    injector: Injector,
    uiConfigProvider: Application.UiConfigProvider
) extends Logging {
  private[this] var errors = List.empty[(String, String, Map[String, String], Option[Throwable])]
  def addError(key: String, msg: String, params: Map[String, String] = Map.empty, ex: Option[Throwable] = None) = {
    errors = errors :+ ((key, msg, params, ex))
  }
  def hasErrors = errors.nonEmpty
  def getErrors = errors

  def cfg(u: Option[SystemUser], admin: Boolean, breadcrumbs: String*) = {
    uiConfigProvider.configForUser(u, admin, NotificationService.getNotifications(u), breadcrumbs: _*)
  }
  def cfgAdmin(u: SystemUser, breadcrumbs: String*) = cfg(u = Some(u), admin = true, breadcrumbs: _*)

  def reload() = {
    try { stop() } catch { case _: Throwable => () }
    errors = Nil
    Await.result(start(), 20.seconds)
  }

  Await.result(start(), 20.seconds)

  private[this] def start() = tracing.topLevelTrace("application.start") { implicit tn =>
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    System.setProperty("user.timezone", "UTC")
    EncryptionUtils.setKey(config.secretKey)
    if (config.metrics.micrometerEnabled) { Instrumented.start(config.metrics.micrometerEngine, "service", config.metrics.micrometerHost) }
    lifecycle.addStopHook(() => Future.successful(stop()))

    try {
      db.open(config.cnf.underlying, tracing)
    } catch {
      case NonFatal(x) =>
        val c = DatabaseConfig.fromConfig(config.cnf.underlying, "database.application")
        val params = Map("username" -> c.username, "database" -> c.database.getOrElse(""))
        addError("database", s"Cannot connect to [${c.database.getOrElse("")}/${c.host}]: ${x.getMessage}", params, Some(x))
    }
    try {
      statusProvider.onAppStartup(this, injector)
    } catch {
      case NonFatal(x) => addError("app", s"Error running application startup code: ${x.getMessage}", Map(), Some(x))
    }

    Future.successful(errors.isEmpty)
  }

  private[this] def stop() = {
    db.close()
    CacheService.close()
    if (config.metrics.tracingEnabled) { tracing.close() }
    if (config.metrics.micrometerEnabled) { Instrumented.stop() }
  }
}
