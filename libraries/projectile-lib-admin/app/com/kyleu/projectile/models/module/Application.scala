package com.kyleu.projectile.models.module

import java.util.TimeZone

import akka.actor.{ActorSystem, CoordinatedShutdown}
import com.google.inject.Injector
import com.google.inject.name.Named
import com.kyleu.projectile.models.auth.AuthEnv
import com.kyleu.projectile.models.config.{Configuration, UiConfig, UserSettings}
import com.kyleu.projectile.models.notification.Notification
import com.kyleu.projectile.models.queries.permission.PermissionQueries
import com.kyleu.projectile.models.user.{SystemUser, SystemUserIdentity}
import com.kyleu.projectile.models.web.TracingWSClient
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.cache.CacheService
import com.kyleu.projectile.services.database._
import com.kyleu.projectile.services.notification.NotificationService
import com.kyleu.projectile.services.status.StatusProvider
import com.kyleu.projectile.services.task.ScheduledTaskService
import com.kyleu.projectile.util.metrics.Instrumented
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}
import com.kyleu.projectile.util.{EncryptionUtils, JsonSerializers, Logging}
import com.mohiva.play.silhouette.api.Silhouette
import io.circe.Json

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.control.NonFatal

object Application {
  abstract class UiConfigProvider() {
    def configForUser(su: Option[SystemUser], notifications: Seq[Notification], breadcrumbs: String*): UiConfig
    def allowRegistration: Boolean = true
    def defaultRole: String = "admin"
    def defaultSettings: Json = JsonSerializers.encoderOps(UserSettings()).asJson
  }
}

@javax.inject.Singleton
class Application @javax.inject.Inject() (
    val config: Configuration,
    val cs: CoordinatedShutdown,
    val actorSystem: ActorSystem,
    val silhouette: Silhouette[AuthEnv],
    val ws: TracingWSClient,
    val tracing: TracingService,
    val db: JdbcDatabase,
    @Named("system") val systemDb: JdbcDatabase,
    statusProvider: StatusProvider,
    injector: Injector,
    uiConfigProvider: Application.UiConfigProvider
) extends Logging {
  val errors = new ApplicationErrors(this)

  def cfg(u: Option[SystemUserIdentity], breadcrumbs: String*)(implicit td: TraceData) = {
    uiConfigProvider.configForUser(u.map(_.user), NotificationService.getNotifications(u.map(_.user)), breadcrumbs: _*)
  }

  def reload(td: TraceData) = {
    try { stop() } catch { case _: Throwable => () }
    errors.clear(td)
    Await.result(start(restart = true), 20.seconds)
    errors.checkTables(td)
    if (ApplicationFeature.enabled(ApplicationFeature.Permission)) {
      try { PermissionService.initialize(systemDb.query(PermissionQueries.getAll())(td))(td) } catch { case _: Throwable => () }
    }
    !errors.hasErrors
  }

  Await.result(start(), 20.seconds)

  private[this] def start(restart: Boolean = false) = tracing.topLevelTrace("application.start") { implicit td =>
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    System.setProperty("user.timezone", "UTC")
    EncryptionUtils.setKey(config.secretKey)
    if (config.metrics.micrometerEnabled) { Instrumented.start(config.metrics.micrometerEngine, "service", config.metrics.micrometerHost) }
    cs.addTask(CoordinatedShutdown.PhaseServiceUnbind, "application-stop") { () =>
      stop()
      Future.successful(akka.Done)
    }
    errors.checkDatabase()
    try {
      statusProvider.onAppStartup(this, injector)
    } catch {
      case NonFatal(x) => errors.addError("app", s"Error running application startup code: ${x.getMessage}", Map(), Some(x))
    }

    Future.successful(!errors.hasErrors)
  }

  private[this] def stop() = {
    if (ApplicationFeature.enabled(ApplicationFeature.Task)) {
      injector.getInstance(classOf[ScheduledTaskService]).stopSchedule()
    }
    actorSystem.terminate()
    db.close()
    CacheService.close()
    if (config.metrics.tracingEnabled) { tracing.close() }
    if (config.metrics.micrometerEnabled) { Instrumented.stop() }
  }
}
