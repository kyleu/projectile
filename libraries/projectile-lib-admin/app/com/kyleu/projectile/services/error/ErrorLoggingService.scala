package com.kyleu.projectile.services.error

import java.util.UUID

import com.kyleu.projectile.models.error.SystemError
import com.kyleu.projectile.models.module.ApplicationFeature
import com.kyleu.projectile.models.queries.error.SystemErrorQueries
import com.kyleu.projectile.services.database.JdbcDatabase
import com.kyleu.projectile.util.tracing.TraceData
import com.kyleu.projectile.util.{ExceptionUtils, Logging}
import javax.inject.Named

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@javax.inject.Singleton
class ErrorLoggingService @javax.inject.Inject() (@Named("system") db: JdbcDatabase)(implicit ec: ExecutionContext) extends Logging {
  def record(userId: Option[UUID], ctx: String, x: Throwable)(implicit td: TraceData) = if (ApplicationFeature.enabled(ApplicationFeature.Error)) {
    val e = SystemError.empty(context = ctx, userId = userId, cls = x.getClass.getName, message = x.getMessage, stacktrace = Some(ExceptionUtils.print(x)))
    db.executeF(SystemErrorQueries.insert(e)).map(_ => ()).recover {
      case ex => log.warn("Error logging exception", ex)
    }.recover {
      case NonFatal(x) =>
        log.error("Error logging exception", x)
        ()
    }
  } else {
    Future.successful(())
  }
}
