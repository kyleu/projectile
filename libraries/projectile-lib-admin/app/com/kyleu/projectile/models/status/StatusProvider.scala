package com.kyleu.projectile.models.status

import com.google.inject.Injector
import com.kyleu.projectile.models.module.Application

trait StatusProvider {
  def onAppStartup(app: Application, injector: Injector): Unit
  def getStatus(app: Application, injector: Injector): AppStatus
}
