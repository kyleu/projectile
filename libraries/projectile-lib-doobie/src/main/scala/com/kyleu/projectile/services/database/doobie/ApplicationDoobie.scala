package com.kyleu.projectile.services.database.doobie

import com.kyleu.projectile.services.database.ApplicationDatabase

object ApplicationDoobie {
  private[this] var doobieOpt: Option[DoobieQueryService] = None
  def doobie = doobieOpt.getOrElse(throw new IllegalStateException("Doobie not initialized"))

  def open() = {
    doobieOpt = Some(new DoobieQueryService(ApplicationDatabase.source))
  }
}
