package com.kyleu.projectile.models.task

import com.kyleu.projectile.services.Credentials
import com.kyleu.projectile.util.tracing.TraceData

import scala.concurrent.Future

abstract class ScheduledTask(val key: String, val title: String, val description: Option[String], val runFrequencySeconds: Int) {
  def run(creds: Credentials, log: String => Unit)(implicit td: TraceData): Future[Boolean]
}
