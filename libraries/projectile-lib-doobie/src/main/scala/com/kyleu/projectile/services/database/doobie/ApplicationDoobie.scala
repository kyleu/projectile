package com.kyleu.projectile.services.database.doobie

import com.kyleu.projectile.services.database.ApplicationDatabase
import com.kyleu.projectile.util.tracing.TracingService

object ApplicationDoobie {
  private[this] var doobieOpt: Option[DoobieQueryService] = None
  def doobie = doobieOpt.getOrElse(throw new IllegalStateException("Doobie not initialized"))

  def open(svc: TracingService) = {
    doobieOpt = Some(new DoobieQueryService(ApplicationDatabase.key, ApplicationDatabase.source))
  }
}
