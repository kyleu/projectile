package models.task

import com.google.inject.Injector
import com.kyleu.projectile.models.task.ScheduledTask
import com.kyleu.projectile.util.Credentials
import com.kyleu.projectile.util.tracing.TraceData

import scala.concurrent.{ExecutionContext, Future}

object TestTask extends ScheduledTask(key = "test", title = "Test", description = Some("A test task!"), runFrequencySeconds = 10) {
  override def run(creds: Credentials, injector: Injector, log: String => Unit)(implicit td: TraceData, ec: ExecutionContext) = {
    Future.successful(false)
  }
}
