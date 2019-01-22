package com.kyleu.projectile.web.util

import com.google.inject.{AbstractModule, Provides}
import com.kyleu.projectile.util.tracing.TraceData
import net.codingwell.scalaguice.ScalaModule
import play.api.mvc.{Flash, Session}

class WebModule extends AbstractModule with ScalaModule {
  @Provides
  def providesErrorActions() = new ErrorHandler.Actions {
    override def badRequest(path: String, error: String)(implicit session: Session, flash: Flash, td: TraceData) = {
      com.kyleu.projectile.web.views.html.error.badRequest(path, error)
    }
    override def serverError(error: String, ex: Option[Throwable])(implicit session: Session, flash: Flash, td: TraceData) = {
      com.kyleu.projectile.web.views.html.error.serverError(error, ex)
    }
    override def notFound(path: String)(implicit session: Session, flash: Flash, td: TraceData) = {
      com.kyleu.projectile.web.views.html.error.notFound(path)
    }
  }
}
