package com.kyleu.projectile.models.task

import com.google.inject.Injector
import com.kyleu.projectile.util.Credentials
import com.kyleu.projectile.util.tracing.TraceData

import scala.concurrent.{ExecutionContext, Future}

abstract class ScheduledTask(val key: String, val title: String, val description: Option[String], val runFrequencyMs: Int, val expectedRuntimeMs: Int) {
  def run(creds: Credentials, injector: Injector, log: String => Unit)(implicit td: TraceData, ec: ExecutionContext): Future[Boolean]
}
