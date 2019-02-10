package com.kyleu.projectile.web.util

import com.google.inject.{AbstractModule, Provides}
import net.codingwell.scalaguice.ScalaModule

class WebModule extends AbstractModule with ScalaModule {
  @Provides
  def providesErrorActions() = new ErrorHandler.Actions()
}
