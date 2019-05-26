package com.kyleu.projectile.models.module

import java.util.TimeZone

import akka.actor.ActorSystem
import com.google.inject.Injector
import com.kyleu.projectile.models.auth.AuthEnv
import com.kyleu.projectile.models.config.{Configuration, UiConfig}
import com.kyleu.projectile.models.notification.Notification
import com.kyleu.projectile.models.queries.permission.PermissionQueries
import com.kyleu.projectile.models.status.StatusProvider
import com.kyleu.projectile.models.user.SystemUser
import com.kyleu.projectile.models.web.TracingWSClient
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.cache.CacheService
import com.kyleu.projectile.services.database._
import com.kyleu.projectile.services.notification.NotificationService
import com.kyleu.projectile.util.metrics.Instrumented
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}
import com.kyleu.projectile.util.{EncryptionUtils, Logging}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.inject.ApplicationLifecycle

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.control.NonFatal

object Application {
  abstract class UiConfigProvider() {
    def configForUser(su: Option[SystemUser], notifications: Seq[Notification], breadcrumbs: String*): UiConfig
    def allowRegistration: Boolean = true
    def defaultRole: String = "admin"
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
  val errors = new ApplicationErrors(this)

  def cfg(u: Option[SystemUser], breadcrumbs: String*) = uiConfigProvider.configForUser(u, NotificationService.getNotifications(u), breadcrumbs: _*)

  def reload() = {
    try { stop() } catch { case _: Throwable => () }
    errors.clear()
    Await.result(start(restart = true), 20.seconds)
    errors.checkTables()
    if (ApplicationFeature.enabled(ApplicationFeature.Permission)) {
      PermissionService.initialize(db.query(PermissionQueries.getAll())(TraceData.noop))
    }
    !errors.hasErrors
  }

  Await.result(start(), 20.seconds)

  private[this] def start(restart: Boolean = false) = tracing.topLevelTrace("application.start") { _ =>
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    System.setProperty("user.timezone", "UTC")
    EncryptionUtils.setKey(config.secretKey)
    if (config.metrics.micrometerEnabled) { Instrumented.start(config.metrics.micrometerEngine, "service", config.metrics.micrometerHost) }
    lifecycle.addStopHook(() => Future.successful(stop()))
    errors.checkDatabase()
    try {
      statusProvider.onAppStartup(this, injector)
    } catch {
      case NonFatal(x) => errors.addError("app", s"Error running application startup code: ${x.getMessage}", Map(), Some(x))
    }

    Future.successful(!errors.hasErrors)
  }

  private[this] def stop() = {
    db.close()
    CacheService.close()
    if (config.metrics.tracingEnabled) { tracing.close() }
    if (config.metrics.micrometerEnabled) { Instrumented.stop() }
  }
}
