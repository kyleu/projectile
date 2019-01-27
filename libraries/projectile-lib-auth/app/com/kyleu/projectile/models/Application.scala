package com.kyleu.projectile.models

import java.util.TimeZone

import akka.actor.ActorSystem
import com.kyleu.projectile.models.auth.AuthEnv
import com.kyleu.projectile.models.database.DatabaseConfig
import com.kyleu.projectile.services.database._
import com.kyleu.projectile.util.metrics.Instrumented
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}
import com.kyleu.projectile.util.{EncryptionUtils, Logging}
import com.kyleu.projectile.web.util.TracingWSClient
import com.mohiva.play.silhouette.api.Silhouette
import play.api.Environment
import play.api.inject.ApplicationLifecycle
import play.api.mvc.Call

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.control.NonFatal

object Application {
  case class Actions(projectName: String, failedRedirect: Call)
}

@javax.inject.Singleton
class Application @javax.inject.Inject() (
    val actions: Application.Actions,
    val config: Configuration,
    val lifecycle: ApplicationLifecycle,
    val playEnv: Environment,
    val actorSystem: ActorSystem,
    val silhouette: Silhouette[AuthEnv],
    val ws: TracingWSClient,
    val tracing: TracingService
) extends Logging {

  val projectName = actions.projectName

  Await.result(start(), 20.seconds)

  private[this] def start() = tracing.topLevelTrace("application.start") { implicit tn =>
    log.info(s"$projectName is starting.")

    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    System.setProperty("user.timezone", "UTC")
    EncryptionUtils.setKey(config.secretKey)

    if (config.metrics.micrometerEnabled) { Instrumented.start(config.metrics.micrometerEngine, projectName, config.metrics.micrometerHost) }

    lifecycle.addStopHook(() => Future.successful(stop()))

    try {
      ApplicationDatabase.open(config.cnf.underlying, tracing)
    } catch {
      case NonFatal(x) =>
        val c = DatabaseConfig.fromConfig(config.cnf.underlying, "database.application")
        throw new IllegalArgumentException(s"Cannot connect to database using [$c]: $x", x)
    }

    Future.successful(true)
  }

  private[this] def stop() = {
    ApplicationDatabase.close()
    if (config.metrics.tracingEnabled) { tracing.close() }
    if (config.metrics.micrometerEnabled) { Instrumented.stop() }
  }
}
