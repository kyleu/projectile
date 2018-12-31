package com.kyleu.projectile.services.database.slick

import com.kyleu.projectile.services.database.ApplicationDatabase
import com.kyleu.projectile.util.tracing.TracingService

object ApplicationSlick {
  private[this] var slickOpt: Option[SlickQueryService] = None
  def slick = slickOpt.getOrElse(throw new IllegalStateException("Slick not initialized."))

  def open(svc: TracingService) = {
    slickOpt = Some(new SlickQueryService(ApplicationDatabase.key, ApplicationDatabase.source, 32, svc))
  }
}
